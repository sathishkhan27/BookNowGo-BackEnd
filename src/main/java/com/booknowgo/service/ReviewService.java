package com.booknowgo.service;

import com.booknowgo.dto.ReviewDto;
import com.booknowgo.entity.Booking;
import com.booknowgo.entity.Hotel;
import com.booknowgo.entity.Review;
import com.booknowgo.entity.User;
import com.booknowgo.exception.BadRequestException;
import com.booknowgo.exception.ResourceNotFoundException;
import com.booknowgo.repository.BookingRepository;
import com.booknowgo.repository.HotelRepository;
import com.booknowgo.repository.ReviewRepository;
import com.booknowgo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final HotelService hotelService;

    @Transactional
    public ReviewDto addReview(Long hotelId, Long userId, ReviewDto request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Booking booking = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId()).orElse(null);
        }

        Review review = Review.builder()
                .hotel(hotel)
                .user(user)
                .booking(booking)
                .overallRating(request.getOverallRating())
                .cleanlinessRating(request.getCleanlinessRating() != null ? request.getCleanlinessRating() : request.getOverallRating())
                .locationRating(request.getLocationRating() != null ? request.getLocationRating() : request.getOverallRating())
                .serviceRating(request.getServiceRating() != null ? request.getServiceRating() : request.getOverallRating())
                .facilitiesRating(request.getFacilitiesRating() != null ? request.getFacilitiesRating() : request.getOverallRating())
                .valueRating(request.getValueRating() != null ? request.getValueRating() : request.getOverallRating())
                .headline(request.getHeadline())
                .comment(request.getComment())
                .status("APPROVED") // Default approved in demo, admin can moderate
                .build();

        review = reviewRepository.save(review);

        // Update hotel average rating & count
        BigDecimal newAvg = reviewRepository.calculateAverageRatingForHotel(hotelId);
        if (newAvg != null) {
            hotel.setAverageRating(newAvg.setScale(1, RoundingMode.HALF_UP));
        }
        hotel.setReviewCount((int) reviewRepository.countByHotelIdAndStatus(hotelId, "APPROVED"));
        hotelRepository.save(hotel);

        return hotelService.mapToReviewDto(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getHotelReviews(Long hotelId) {
        return reviewRepository.findByHotelIdAndStatusOrderByCreatedAtDesc(hotelId, "APPROVED")
                .stream()
                .map(hotelService::mapToReviewDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void moderateReview(Long reviewId, String status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setStatus(status.toUpperCase());
        reviewRepository.save(review);
    }
}
