package com.booknowgo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelDetailDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Integer starRating;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private String address;
    private String postalCode;
    private String city;
    private String state;
    private String country;
    private String area;
    private String landmark;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String checkInTime;
    private String checkOutTime;
    private String cancellationPolicy;
    private String primaryImageUrl;
    private List<String> images;
    private List<String> amenities;
    private List<RoomDto> rooms;
    private List<ReviewDto> reviews;
    private boolean isFeatured;
    private BigDecimal startingPrice;
    @Builder.Default
    private String currency = "INR";
}
