package com.booknowgo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in must be today or in the future")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out must be in the future")
    private LocalDate checkOutDate;

    @Min(value = 1, message = "At least 1 room is required")
    @Builder.Default
    private Integer numberOfRooms = 1;

    @Min(value = 1, message = "At least 1 adult is required")
    @Builder.Default
    private Integer adults = 1;

    @Builder.Default
    private Integer children = 0;

    @NotBlank(message = "Guest full name is required")
    private String guestName;

    @NotBlank(message = "Guest email is required")
    @Email(message = "Valid email is required")
    private String guestEmail;

    @NotBlank(message = "Guest phone number is required")
    private String guestPhone;

    private String specialRequests;

    private String couponCode;

    @NotBlank(message = "Payment method is required")
    @Builder.Default
    private String paymentMethod = "CREDIT_CARD"; // CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, WALLET
}
