package com.booknowgo.service;

import com.booknowgo.dto.CouponValidationDto;
import com.booknowgo.entity.Coupon;
import com.booknowgo.exception.BadRequestException;
import com.booknowgo.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public List<Coupon> getActiveCoupons() {
        return couponRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public CouponValidationDto validateCoupon(String code, BigDecimal amount) {
        if (code == null || code.trim().isEmpty()) {
            return CouponValidationDto.builder()
                    .valid(false)
                    .message("Coupon code cannot be blank")
                    .build();
        }

        var opt = couponRepository.findByCodeIgnoreCase(code.trim());
        if (opt.isEmpty()) {
            return CouponValidationDto.builder()
                    .valid(false)
                    .code(code)
                    .message("Invalid coupon code")
                    .build();
        }

        Coupon coupon = opt.get();
        if (!coupon.isValid(amount)) {
            String reason = "Coupon is not applicable";
            if (!coupon.isActive()) reason = "Coupon is expired or inactive";
            else if (coupon.getTimesUsed() >= coupon.getUsageLimit()) reason = "Coupon usage limit has been exceeded";
            else if (coupon.getMinBookingAmount() != null && amount.compareTo(coupon.getMinBookingAmount()) < 0) {
                reason = "Minimum booking amount of ₹" + coupon.getMinBookingAmount() + " required";
            }

            return CouponValidationDto.builder()
                    .valid(false)
                    .code(coupon.getCode())
                    .message(reason)
                    .currency("INR")
                    .build();
        }

        BigDecimal discount = coupon.calculateDiscount(amount);

        return CouponValidationDto.builder()
                .valid(true)
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .calculatedDiscount(discount)
                .message("Coupon applied successfully! You save ₹" + discount)
                .currency("INR")
                .build();
    }
}
