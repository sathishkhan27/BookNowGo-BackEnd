package com.booknowgo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {
    private String refundReference;
    private String bookingReference;
    private BigDecimal originalAmount;
    private BigDecimal refundAmount;
    private BigDecimal cancellationFee;
    private String refundStatus;
    private String reason;
    private LocalDateTime processedAt;
}
