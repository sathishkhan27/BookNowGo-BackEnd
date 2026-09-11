package com.booknowgo.service;

import com.booknowgo.dto.AdminStatsDto;
import com.booknowgo.dto.BookingResponse;
import com.booknowgo.dto.HotelDto;
import com.booknowgo.dto.UserDto;
import com.booknowgo.entity.Coupon;
import com.booknowgo.entity.Hotel;
import com.booknowgo.entity.User;
import com.booknowgo.exception.ResourceNotFoundException;
import com.booknowgo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final LocationRepository locationRepository;
    private final CouponRepository couponRepository;
    private final HotelService hotelService;
    private final BookingService bookingService;
    private final AuthService authService;

    @Value("${app.pricing.platform-commission-percentage:15.0}")
    private BigDecimal platformCommissionPercentage;

    @Transactional(readOnly = true)
    public AdminStatsDto getAdminStats() {
        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.countCustomers();
        long totalOwners = userRepository.countOwners();
        long totalHotels = hotelRepository.count();
        long pendingApprovals = hotelRepository.countByStatus("PENDING");
        long totalBookings = bookingRepository.count();

        BigDecimal grossRevenue = bookingRepository.calculateTotalGrossRevenue();
        BigDecimal commission = grossRevenue.multiply(platformCommissionPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        List<HotelDto> pendingHotels = hotelRepository.findByStatus("PENDING")
                .stream()
                .map(hotelService::mapToHotelDto)
                .collect(Collectors.toList());

        List<com.booknowgo.entity.Booking> allBookings = bookingRepository.findAll();

        // Dynamic Booking Trends (Last 6 Months)
        List<Map<String, Object>> bookingTrends = new ArrayList<>();
        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        java.time.format.DateTimeFormatter monthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

        for (int i = 5; i >= 0; i--) {
            java.time.YearMonth targetMonth = currentMonth.minusMonths(i);
            long count = allBookings.stream()
                    .filter(b -> b.getCreatedAt() != null && java.time.YearMonth.from(b.getCreatedAt()).equals(targetMonth))
                    .count();
            Map<String, Object> point = new HashMap<>();
            point.put("month", targetMonth.format(monthFormatter));
            point.put("bookings", count);
            bookingTrends.add(point);
        }

        // Dynamic Top Destinations (derived from actual bookings or approved hotels)
        List<Map<String, Object>> topDestinations = new ArrayList<>();
        Map<String, Long> cityCounts = allBookings.stream()
                .filter(b -> b.getHotel() != null && b.getHotel().getLocation() != null && b.getHotel().getLocation().getCity() != null)
                .collect(Collectors.groupingBy(b -> b.getHotel().getLocation().getCity(), Collectors.counting()));

        if (cityCounts.isEmpty()) {
            cityCounts = hotelRepository.findByStatus("APPROVED").stream()
                    .filter(h -> h.getLocation() != null && h.getLocation().getCity() != null)
                    .collect(Collectors.groupingBy(h -> h.getLocation().getCity(), Collectors.counting()));
        }

        long totalCityItems = cityCounts.values().stream().mapToLong(Long::longValue).sum();
        if (totalCityItems > 0) {
            cityCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> {
                        int share = (int) Math.round(((double) entry.getValue() / totalCityItems) * 100);
                        topDestinations.add(Map.of("city", entry.getKey(), "share", share));
                    });
        }

        return AdminStatsDto.builder()
                .totalUsers(totalUsers)
                .totalCustomers(totalCustomers)
                .totalOwners(totalOwners)
                .totalHotels(totalHotels)
                .pendingHotelApprovals(pendingApprovals)
                .totalBookings(totalBookings)
                .totalGrossBookingsAmount(grossRevenue)
                .totalPlatformCommission(commission)
                .bookingTrends(bookingTrends)
                .topDestinations(topDestinations)
                .pendingHotels(pendingHotels)
                .build();
    }

    @Transactional
    public HotelDto updateHotelStatus(Long hotelId, String status) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
        hotel.setStatus(status.toUpperCase());
        hotel = hotelRepository.save(hotel);
        return hotelService.mapToHotelDto(hotel);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(bookingService::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(authService::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }
}
