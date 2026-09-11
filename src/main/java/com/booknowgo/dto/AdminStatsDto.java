package com.booknowgo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsDto {
    private long totalUsers;
    private long totalCustomers;
    private long totalOwners;
    private long totalHotels;
    private long pendingHotelApprovals;
    private long totalBookings;
    private BigDecimal totalGrossBookingsAmount;
    private BigDecimal totalPlatformCommission;
    private List<Map<String, Object>> bookingTrends;
    private List<Map<String, Object>> topDestinations;
    private List<HotelDto> pendingHotels;
}
