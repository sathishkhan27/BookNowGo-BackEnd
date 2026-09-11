package com.booknowgo.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponValidationDto {
    private boolean valid;
    private String code;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal calculatedDiscount;
    private String message;
    @Builder.Default
    private String currency = "INR";
}
