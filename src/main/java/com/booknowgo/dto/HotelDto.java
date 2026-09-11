package com.booknowgo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Integer starRating;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private String address;
    private String city;
    private String state;
    private String country;
    private String area;
    private String landmark;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String primaryImageUrl;
    private List<String> images;
    private List<String> amenities;
    private BigDecimal startingPrice;
    private BigDecimal originalPrice;
    private Integer discountPercentage;
    private boolean freeCancellation;
    private String cancellationPolicy;
    private String status;
    private boolean isFeatured;
    @Builder.Default
    private String currency = "INR";
}
