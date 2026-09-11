package com.booknowgo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDto {
    private Long id;
    private Long hotelId;
    private String name;
    private String roomType;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal discountedPrice;
    private Integer discountPercentage;
    private Integer maxAdults;
    private Integer maxChildren;
    private String bedType;
    private Integer roomSizeSqft;
    private String mealPlan;
    private String cancellationPolicy;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private String primaryImageUrl;
    private List<String> images;
    private List<String> amenities;
    private boolean isActive;
    @Builder.Default
    private String currency = "INR";
}
