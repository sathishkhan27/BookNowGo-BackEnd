package com.booknowgo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 20)
    private String discountType; // PERCENTAGE, FLAT

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minBookingAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validTo;

    @Builder.Default
    private Integer usageLimit = 1000;

    @Builder.Default
    private Integer timesUsed = 0;

    @Builder.Default
    private boolean isActive = true;

    public boolean isValid(BigDecimal bookingAmount) {
        if (!isActive) return false;
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(validFrom) || now.isAfter(validTo)) return false;
        if (timesUsed >= usageLimit) return false;
        if (minBookingAmount != null && bookingAmount.compareTo(minBookingAmount) < 0) return false;
        return true;
    }

    public BigDecimal calculateDiscount(BigDecimal baseAmount) {
        if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
            BigDecimal discount = baseAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
            if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                return maxDiscountAmount;
            }
            return discount;
        } else {
            return discountValue.min(baseAmount);
        }
    }
}
