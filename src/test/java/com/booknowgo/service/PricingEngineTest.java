package com.booknowgo.service;

import com.booknowgo.dto.FareBreakdownDto;
import com.booknowgo.entity.Coupon;
import com.booknowgo.entity.Room;
import com.booknowgo.repository.RoomInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingEngineTest {

    @Mock
    private RoomInventoryRepository roomInventoryRepository;

    @InjectMocks
    private PricingEngine pricingEngine;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pricingEngine, "taxPercentage", BigDecimal.valueOf(12.0));
        ReflectionTestUtils.setField(pricingEngine, "serviceFeePercentage", BigDecimal.valueOf(5.0));
        ReflectionTestUtils.setField(pricingEngine, "currency", "INR");
    }

    @Test
    void testCalculateFareInInrWithoutCoupon() {
        Room room = Room.builder()
                .id(1L)
                .name("Deluxe Lake View")
                .basePrice(BigDecimal.valueOf(5000.00))
                .build();

        LocalDate checkIn = LocalDate.of(2026, 10, 5); // Monday
        LocalDate checkOut = LocalDate.of(2026, 10, 7); // Wednesday (2 weekday nights)

        when(roomInventoryRepository.findByRoomAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        FareBreakdownDto fare = pricingEngine.calculateFare(room, checkIn, checkOut, 1, null);

        assertNotNull(fare);
        assertEquals("INR", fare.getCurrency());
        assertEquals(2, fare.getNumberOfNights());
        assertEquals(BigDecimal.valueOf(5000.00), fare.getBasePricePerNight());

        // Total room base: 5000 * 2 = 10000.00
        assertEquals(new BigDecimal("10000.00"), fare.getTotalRoomBasePrice());

        // Dynamic promo discount is 0.00 (no hardcoded discount)
        assertEquals(new BigDecimal("0.00"), fare.getDiscountAmount());

        // Taxable: 10000.00
        assertEquals(new BigDecimal("10000.00"), fare.getTaxableAmount());

        // 12% GST tax: 1200.00
        assertEquals(new BigDecimal("1200.00"), fare.getTaxAmount());

        // 5% service fee: 500.00
        assertEquals(new BigDecimal("500.00"), fare.getServiceFee());

        // Final total: 10000 + 1200 + 500 = 11700.00
        assertEquals(new BigDecimal("11700.00"), fare.getFinalTotalAmount());
    }

    @Test
    void testCalculateFareWithFlatCouponInr() {
        Room room = Room.builder()
                .id(1L)
                .name("Heritage Room")
                .basePrice(BigDecimal.valueOf(4000.00))
                .build();

        Coupon coupon = Coupon.builder()
                .code("WELCOME500")
                .discountType("FLAT")
                .discountValue(BigDecimal.valueOf(500.00))
                .minBookingAmount(BigDecimal.valueOf(2000.00))
                .maxDiscountAmount(BigDecimal.valueOf(500.00))
                .validFrom(java.time.LocalDateTime.now().minusDays(1))
                .validTo(java.time.LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();

        LocalDate checkIn = LocalDate.of(2026, 10, 5); // Monday
        LocalDate checkOut = LocalDate.of(2026, 10, 6); // Tuesday (1 night)

        when(roomInventoryRepository.findByRoomAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        FareBreakdownDto fare = pricingEngine.calculateFare(room, checkIn, checkOut, 1, coupon);

        assertEquals("INR", fare.getCurrency());
        assertEquals(new BigDecimal("500.00"), fare.getCouponDiscount());
        assertEquals("WELCOME500", fare.getCouponCode());
    }
}
