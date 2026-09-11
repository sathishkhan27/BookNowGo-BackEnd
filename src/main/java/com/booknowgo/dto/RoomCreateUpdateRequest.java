package com.booknowgo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomCreateUpdateRequest {
    @NotBlank(message = "Room name is required")
    private String name;

    @NotBlank(message = "Room type is required")
    private String roomType;

    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "1.0", message = "Base price must be positive")
    private BigDecimal basePrice;

    @Min(value = 1, message = "At least 1 adult required")
    @Builder.Default
    private Integer maxAdults = 2;

    @Builder.Default
    private Integer maxChildren = 1;

    private String bedType;
    private Integer roomSizeSqft;
    private String mealPlan;
    private String cancellationPolicy;

    @Min(value = 1, message = "Total quantity must be at least 1")
    @Builder.Default
    private Integer totalQuantity = 5;

    private List<String> imageUrls;
    private List<String> amenities;
}
