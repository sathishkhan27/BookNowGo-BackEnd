package com.booknowgo.controller;

import com.booknowgo.dto.ApiResponse;
import com.booknowgo.dto.HotelDto;
import com.booknowgo.security.UserPrincipal;
import com.booknowgo.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Customer saved hotels")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get user's saved wishlist hotels")
    public ResponseEntity<ApiResponse<List<HotelDto>>> getWishlist(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<HotelDto> wishlist = wishlistService.getUserWishlist(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(wishlist, "Wishlist retrieved"));
    }

    @GetMapping("/ids")
    @Operation(summary = "Get IDs of all saved hotels in user wishlist")
    public ResponseEntity<ApiResponse<List<Long>>> getWishlistHotelIds(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<Long> ids = wishlistService.getUserWishlistHotelIds(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(ids, "Wishlist hotel IDs retrieved"));
    }

    @PostMapping("/toggle/{hotelId}")
    @Operation(summary = "Toggle a hotel in/out of the user's wishlist")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleWishlist(
            @PathVariable Long hotelId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        boolean added = wishlistService.toggleWishlist(userPrincipal.getId(), hotelId);
        String msg = added ? "Added hotel to wishlist" : "Removed hotel from wishlist";
        return ResponseEntity.ok(ApiResponse.ok(Map.of("saved", added, "hotelId", hotelId), msg));
    }
}
