package com.booknowgo.service;

import com.booknowgo.dto.FareBreakdownDto;
import com.booknowgo.entity.Coupon;
import com.booknowgo.entity.Room;
import com.booknowgo.entity.RoomInventory;
import com.booknowgo.repository.RoomInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingEngine {

    private final RoomInventoryRepository roomInventoryRepository;

    @Value("${app.pricing.currency:INR}")
    private String currency;

    @Value("${app.pricing.tax-percentage:12.0}")
    private BigDecimal taxPercentage;

    @Value("${app.pricing.service-fee-percentage:5.0}")
    private BigDecimal serviceFeePercentage;

    public FareBreakdownDto calculateFare(Room room, LocalDate checkIn, LocalDate checkOut, int numberOfRooms, Coupon coupon) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            nights = 1;
        }

        BigDecimal basePricePerNight = room.getBasePrice();

        // Calculate dynamic night pricing with inventory modifiers or weekend rates
        List<RoomInventory> customInventories = roomInventoryRepository.findByRoomAndDateRange(room.getId(), checkIn, checkOut.minusDays(1));
        
        BigDecimal totalRoomBase = BigDecimal.ZERO;
        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            BigDecimal nightPrice = basePricePerNight;

            // Check custom inventory modifier
            var matchingInv = customInventories.stream()
                    .filter(inv -> inv.getInventoryDate().equals(currentDate))
                    .findFirst();

            if (matchingInv.isPresent() && matchingInv.get().getPriceModifier() != null) {
                nightPrice = nightPrice.add(matchingInv.get().getPriceModifier());
            } else if (currentDate.getDayOfWeek() == DayOfWeek.FRIDAY || currentDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                // Weekend bump 10%
                nightPrice = nightPrice.multiply(BigDecimal.valueOf(1.10));
            }
            totalRoomBase = totalRoomBase.add(nightPrice);
        }

        totalRoomBase = totalRoomBase.multiply(BigDecimal.valueOf(numberOfRooms)).setScale(2, RoundingMode.HALF_UP);

        // Default promo discount is zero; discounts derive dynamically from coupons
        BigDecimal discountAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountedSubtotal = totalRoomBase;

        // Coupon application
        BigDecimal couponDiscount = BigDecimal.ZERO;
        String couponCode = null;
        if (coupon != null && coupon.isValid(discountedSubtotal)) {
            couponCode = coupon.getCode();
            couponDiscount = coupon.calculateDiscount(discountedSubtotal).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal taxableAmount = discountedSubtotal.subtract(couponDiscount).max(BigDecimal.ZERO);

        BigDecimal taxAmount = taxableAmount.multiply(taxPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal serviceFee = taxableAmount.multiply(serviceFeePercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal finalTotal = taxableAmount.add(taxAmount).add(serviceFee).setScale(2, RoundingMode.HALF_UP);

        return FareBreakdownDto.builder()
                .basePricePerNight(basePricePerNight)
                .numberOfNights((int) nights)
                .numberOfRooms(numberOfRooms)
                .totalRoomBasePrice(totalRoomBase)
                .discountAmount(discountAmount)
                .couponCode(couponCode)
                .couponDiscount(couponDiscount)
                .taxableAmount(taxableAmount)
                .taxPercentage(taxPercentage)
                .taxAmount(taxAmount)
                .serviceFeePercentage(serviceFeePercentage)
                .serviceFee(serviceFee)
                .finalTotalAmount(finalTotal)
                .currency(currency != null ? currency : "INR")
                .build();
    }
}
