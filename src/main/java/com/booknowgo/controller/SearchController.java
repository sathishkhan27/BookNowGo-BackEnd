package com.booknowgo.controller;

import com.booknowgo.dto.ApiResponse;
import com.booknowgo.dto.FareBreakdownDto;
import com.booknowgo.dto.HotelDto;
import com.booknowgo.dto.HotelSearchRequest;
import com.booknowgo.entity.Coupon;
import com.booknowgo.entity.Room;
import com.booknowgo.exception.ResourceNotFoundException;
import com.booknowgo.repository.CouponRepository;
import com.booknowgo.repository.RoomRepository;
import com.booknowgo.service.PricingEngine;
import com.booknowgo.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Hotel discovery, filters, and dynamic fare preview")
public class SearchController {

    private final SearchService searchService;
    private final PricingEngine pricingEngine;
    private final RoomRepository roomRepository;
    private final CouponRepository couponRepository;

    @GetMapping
    @Operation(summary = "Search hotels by location, date, price, rating, and amenities")
    public ResponseEntity<ApiResponse<List<HotelDto>>> searchHotels(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Integer adults,
            @RequestParam(required = false) Integer children,
            @RequestParam(required = false) Integer rooms,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer starRating,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) List<String> amenities,
            @RequestParam(required = false) Boolean freeCancellation,
            @RequestParam(required = false, defaultValue = "recommended") String sortBy) {

        HotelSearchRequest criteria = HotelSearchRequest.builder()
                .query(query)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .adults(adults)
                .children(children)
                .rooms(rooms != null ? rooms : 1)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .starRating(starRating)
                .minRating(minRating)
                .amenities(amenities)
                .freeCancellation(freeCancellation)
                .sortBy(sortBy)
                .build();

        List<HotelDto> results = searchService.searchHotels(criteria);
        return ResponseEntity.ok(ApiResponse.ok(results, "Found " + results.size() + " matching hotels"));
    }

    @GetMapping("/fare-preview")
    @Operation(summary = "Calculate exact fare breakdown with taxes, fees, and optional coupon")
    public ResponseEntity<ApiResponse<FareBreakdownDto>> getFarePreview(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") int rooms,
            @RequestParam(required = false) String couponCode) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        Coupon coupon = null;
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            coupon = couponRepository.findByCodeIgnoreCase(couponCode.trim()).orElse(null);
        }

        FareBreakdownDto fare = pricingEngine.calculateFare(room, checkIn, checkOut, rooms, coupon);
        return ResponseEntity.ok(ApiResponse.ok(fare, "Fare preview calculated"));
    }
}
