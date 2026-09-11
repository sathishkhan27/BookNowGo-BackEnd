package com.booknowgo.controller;

import com.booknowgo.dto.ApiResponse;
import com.booknowgo.dto.CouponValidationDto;
import com.booknowgo.entity.Coupon;
import com.booknowgo.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "Coupon vouchers and discounts")
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/active")
    @Operation(summary = "Get list of active promotional coupons and offers")
    public ResponseEntity<ApiResponse<List<Coupon>>> getActiveCoupons() {
        List<Coupon> coupons = couponService.getActiveCoupons();
        return ResponseEntity.ok(ApiResponse.ok(coupons, "Active coupons retrieved"));
    }

    @GetMapping("/validate/{code}")
    @Operation(summary = "Validate coupon code against booking subtotal")
    public ResponseEntity<ApiResponse<CouponValidationDto>> validateCoupon(
            @PathVariable String code,
            @RequestParam(defaultValue = "100.00") BigDecimal amount) {

        CouponValidationDto result = couponService.validateCoupon(code, amount);
        return ResponseEntity.ok(ApiResponse.ok(result, result.getMessage()));
    }
}
