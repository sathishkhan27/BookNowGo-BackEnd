package com.booknowgo.service;

import com.booknowgo.dto.AuthRequest;
import com.booknowgo.dto.AuthResponse;
import com.booknowgo.dto.RegisterRequest;
import com.booknowgo.dto.UserDto;
import com.booknowgo.entity.Role;
import com.booknowgo.entity.User;
import com.booknowgo.exception.BadRequestException;
import com.booknowgo.exception.ResourceNotFoundException;
import com.booknowgo.repository.RoleRepository;
import com.booknowgo.repository.UserRepository;
import com.booknowgo.security.JwtTokenProvider;
import com.booknowgo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        final String targetRole = "HOTEL_OWNER".equalsIgnoreCase(request.getRole()) 
                ? "ROLE_HOTEL_OWNER" 
                : "ROLE_CUSTOMER";

        Role role = roleRepository.findByName(targetRole)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(targetRole)
                        .description("Default role")
                        .build()));

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phoneNumber(request.getPhoneNumber())
                .avatarUrl("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80")
                .isActive(true)
                .isVerified(true)
                .roles(new HashSet<>(Collections.singletonList(role)))
                .build();

        user = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);

        return AuthResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .expiresIn(86400000L)
                .user(mapToUserDto(user))
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return AuthResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .expiresIn(86400000L)
                .user(mapToUserDto(user))
                .build();
    }

    public UserDto getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToUserDto(user);
    }

    public UserDto mapToUserDto(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .roles(roles)
                .build();
    }
}
