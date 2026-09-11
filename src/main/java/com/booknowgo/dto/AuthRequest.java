package com.booknowgo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email format required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
