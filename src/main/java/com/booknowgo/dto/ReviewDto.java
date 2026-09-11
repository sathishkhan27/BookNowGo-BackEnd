package com.booknowgo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    private Long id;
    private Long hotelId;
    private String hotelName;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long bookingId;

    @NotNull(message = "Overall rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be between 1.0 and 10.0")
    @DecimalMax(value = "10.0", message = "Rating must be between 1.0 and 10.0")
    private BigDecimal overallRating;

    private BigDecimal cleanlinessRating;
    private BigDecimal locationRating;
    private BigDecimal serviceRating;
    private BigDecimal facilitiesRating;
    private BigDecimal valueRating;
    private String headline;
    private String comment;
    private String status;
    private LocalDateTime createdAt;
}
