package com.booknowgo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private Long hotelId;
    private String hotelName;
    private String hotelAddress;
    private String hotelCity;
    private String hotelImage;
    private String checkInTime;
    private String checkOutTime;
    private Long roomId;
    private String roomName;
    private String roomType;
    private String bedType;
    private String mealPlan;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer totalNights;
    private Integer numberOfRooms;
    private Integer adults;
    private Integer children;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String specialRequests;
    private FareBreakdownDto fareBreakdown;
    private BigDecimal totalAmount;
    private String bookingStatus;
    private String paymentStatus;
    private String paymentMethod;
    private String transactionReference;
    private String cancellationPolicy;
    private LocalDateTime createdAt;
    @Builder.Default
    private String currency = "INR";
}
