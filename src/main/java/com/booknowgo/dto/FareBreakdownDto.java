package com.booknowgo.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareBreakdownDto {
    private BigDecimal basePricePerNight;
    private Integer numberOfNights;
    private Integer numberOfRooms;
    private BigDecimal totalRoomBasePrice;
    private BigDecimal discountAmount;
    private String couponCode;
    private BigDecimal couponDiscount;
    private BigDecimal taxableAmount;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal serviceFeePercentage;
    private BigDecimal serviceFee;
    private BigDecimal finalTotalAmount;
    @Builder.Default
    private String currency = "INR";
}
