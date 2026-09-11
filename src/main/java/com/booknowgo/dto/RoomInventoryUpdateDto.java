package com.booknowgo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomInventoryUpdateDto {
    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Available count is required")
    private Integer availableCount;

    private Integer blockedCount;
    private BigDecimal priceModifier;
}
