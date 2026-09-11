package com.booknowgo.controller;

import com.booknowgo.dto.ApiResponse;
import com.booknowgo.dto.BookingRequest;
import com.booknowgo.dto.BookingResponse;
import com.booknowgo.dto.CancellationRequest;
import com.booknowgo.dto.RefundResponse;
import com.booknowgo.security.UserPrincipal;
import com.booknowgo.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Room booking creation, history, vouchers, and cancellations")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new hotel room booking and process payment")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long customerId = userPrincipal != null ? userPrincipal.getId() : 1L; // Fallback to customer if authenticated
        BookingResponse response = bookingService.createBooking(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Booking confirmed successfully"));
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "Get bookings of the authenticated customer")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<BookingResponse> bookings = bookingService.getCustomerBookings(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(bookings, "Customer bookings retrieved"));
    }

    @GetMapping("/{reference}")
    @Operation(summary = "Get booking confirmation voucher by reference")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByReference(
            @PathVariable String reference) {

        BookingResponse booking = bookingService.getBookingByReference(reference);
        return ResponseEntity.ok(ApiResponse.ok(booking, "Booking voucher retrieved"));
    }

    @PostMapping("/{reference}/cancel")
    @Operation(summary = "Cancel booking and process refund based on policy")
    public ResponseEntity<ApiResponse<RefundResponse>> cancelBooking(
            @PathVariable String reference,
            @Valid @RequestBody CancellationRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        RefundResponse refund = bookingService.cancelBooking(reference, userPrincipal.getId(), request.getReason());
        return ResponseEntity.ok(ApiResponse.ok(refund, "Booking cancelled and refund processed"));
    }
}
