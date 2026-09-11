package com.booknowgo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelSearchRequest {
    private String query; // City, area, hotel name, landmark
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer adults;
    private Integer children;
    private Integer rooms;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer starRating;
    private BigDecimal minRating;
    private List<String> amenities;
    private Boolean freeCancellation;
    private String sortBy; // recommended, price_asc, price_desc, rating, stars
}
