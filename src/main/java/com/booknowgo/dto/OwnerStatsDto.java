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
public class OwnerStatsDto {
    private long totalBookings;
    private long todayBookings;
    private long upcomingBookings;
    private BigDecimal totalRevenue;
    private Double occupancyRate;
    private long cancellationCount;
    private Double averageRating;
    private List<Map<String, Object>> monthlyRevenue;
    private List<BookingResponse> recentBookings;
}
