package com.booknowgo.controller;

import com.booknowgo.dto.AdminStatsDto;
import com.booknowgo.dto.ApiResponse;
import com.booknowgo.dto.BookingResponse;
import com.booknowgo.dto.HotelDto;
import com.booknowgo.dto.UserDto;
import com.booknowgo.entity.Coupon;
import com.booknowgo.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Portal", description = "System oversight, approvals, analytics, and coupons")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    @Operation(summary = "Get global platform metrics, booking trends, and revenue")
    public ResponseEntity<ApiResponse<AdminStatsDto>> getAdminStats() {
        AdminStatsDto stats = adminService.getAdminStats();
        return ResponseEntity.ok(ApiResponse.ok(stats, "Admin analytics retrieved"));
    }

    @PutMapping("/hotels/{hotelId}/status")
    @Operation(summary = "Approve, reject, or suspend a hotel listing")
    public ResponseEntity<ApiResponse<HotelDto>> updateHotelStatus(
            @PathVariable Long hotelId,
            @RequestBody Map<String, String> body) {

        String status = body.getOrDefault("status", "APPROVED");
        HotelDto updated = adminService.updateHotelStatus(hotelId, status);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Hotel status updated to " + status));
    }

    @GetMapping("/bookings")
    @Operation(summary = "Get global booking transactions across all hotels")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        List<BookingResponse> bookings = adminService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.ok(bookings, "All platform bookings retrieved"));
    }

    @GetMapping("/users")
    @Operation(summary = "List all registered platform users")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.ok(users, "Platform users retrieved"));
    }

    @GetMapping("/coupons")
    @Operation(summary = "List all system coupons")
    public ResponseEntity<ApiResponse<List<Coupon>>> getAllCoupons() {
        List<Coupon> coupons = adminService.getAllCoupons();
        return ResponseEntity.ok(ApiResponse.ok(coupons, "System coupons retrieved"));
    }

    @PostMapping("/coupons")
    @Operation(summary = "Create a new discount coupon")
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(@Valid @RequestBody Coupon coupon) {
        Coupon created = adminService.createCoupon(coupon);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Coupon created successfully"));
    }
}
