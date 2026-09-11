package com.booknowgo.service;

import com.booknowgo.dto.HotelDto;
import com.booknowgo.entity.Hotel;
import com.booknowgo.entity.User;
import com.booknowgo.entity.Wishlist;
import com.booknowgo.exception.ResourceNotFoundException;
import com.booknowgo.repository.HotelRepository;
import com.booknowgo.repository.UserRepository;
import com.booknowgo.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final HotelService hotelService;

    @Transactional
    public boolean toggleWishlist(Long userId, Long hotelId) {
        if (wishlistRepository.existsByUserIdAndHotelId(userId, hotelId)) {
            wishlistRepository.deleteByUserIdAndHotelId(userId, hotelId);
            return false;
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            Hotel hotel = hotelRepository.findById(hotelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .hotel(hotel)
                    .build();
            wishlistRepository.save(wishlist);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public List<HotelDto> getUserWishlist(Long userId) {
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(w -> hotelService.mapToHotelDto(w.getHotel()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Long> getUserWishlistHotelIds(Long userId) {
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(w -> w.getHotel().getId())
                .collect(Collectors.toList());
    }
}
