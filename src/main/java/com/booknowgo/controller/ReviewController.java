package com.booknowgo.controller;

import com.booknowgo.dto.ApiResponse;
import com.booknowgo.dto.ReviewDto;
import com.booknowgo.security.UserPrincipal;
import com.booknowgo.service.ReviewService;
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
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Hotel reviews and ratings")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/hotel/{hotelId}")
    @Operation(summary = "Get verified reviews for a hotel")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getHotelReviews(@PathVariable Long hotelId) {
        List<ReviewDto> reviews = reviewService.getHotelReviews(hotelId);
        return ResponseEntity.ok(ApiResponse.ok(reviews, "Reviews retrieved successfully"));
    }

    @PostMapping("/hotel/{hotelId}/add")
    @Operation(summary = "Submit a review for a hotel after stay")
    public ResponseEntity<ApiResponse<ReviewDto>> addReview(
            @PathVariable Long hotelId,
            @Valid @RequestBody ReviewDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        ReviewDto review = reviewService.addReview(hotelId, userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(review, "Review submitted successfully"));
    }
}
