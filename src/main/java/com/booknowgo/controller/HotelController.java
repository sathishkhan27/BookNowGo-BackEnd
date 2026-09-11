package com.booknowgo.controller;

import com.booknowgo.dto.ApiResponse;
import com.booknowgo.dto.HotelDetailDto;
import com.booknowgo.dto.HotelDto;
import com.booknowgo.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotels", description = "Hotel discovery and details")
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    @Operation(summary = "Get all approved hotels")
    public ResponseEntity<ApiResponse<List<HotelDto>>> getAllHotels() {
        List<HotelDto> hotels = hotelService.getAllApprovedHotels();
        return ResponseEntity.ok(ApiResponse.ok(hotels, "Hotels retrieved successfully"));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get curated featured hotels")
    public ResponseEntity<ApiResponse<List<HotelDto>>> getFeaturedHotels() {
        List<HotelDto> hotels = hotelService.getFeaturedHotels();
        return ResponseEntity.ok(ApiResponse.ok(hotels, "Featured hotels retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hotel details by ID")
    public ResponseEntity<ApiResponse<HotelDetailDto>> getHotelById(@PathVariable Long id) {
        HotelDetailDto hotel = hotelService.getHotelById(id);
        return ResponseEntity.ok(ApiResponse.ok(hotel, "Hotel details retrieved successfully"));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get hotel details by SEO slug")
    public ResponseEntity<ApiResponse<HotelDetailDto>> getHotelBySlug(@PathVariable String slug) {
        HotelDetailDto hotel = hotelService.getHotelBySlug(slug);
        return ResponseEntity.ok(ApiResponse.ok(hotel, "Hotel details retrieved successfully"));
    }
}
