package com.booknowgo.dto;

import jakarta.validation.constraints.Max;
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
public class HotelCreateUpdateRequest {
    @NotBlank(message = "Hotel name is required")
    private String name;

    private String description;

    @Min(value = 1, message = "Star rating must be 1-5")
    @Max(value = 5, message = "Star rating must be 1-5")
    private Integer starRating;

    @NotBlank(message = "Address is required")
    private String address;

    private String postalCode;

    @NotNull(message = "Location ID is required")
    private Long locationId;

    private String checkInTime;
    private String checkOutTime;
    private String cancellationPolicy;

    private List<String> imageUrls;
    private List<String> amenities;
}
