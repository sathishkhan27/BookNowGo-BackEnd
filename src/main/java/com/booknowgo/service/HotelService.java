package com.booknowgo.service;

import com.booknowgo.dto.HotelDetailDto;
import com.booknowgo.dto.HotelDto;
import com.booknowgo.dto.ReviewDto;
import com.booknowgo.dto.RoomDto;
import com.booknowgo.entity.*;
import com.booknowgo.exception.ResourceNotFoundException;
import com.booknowgo.repository.HotelRepository;
import com.booknowgo.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<HotelDto> getFeaturedHotels() {
        return hotelRepository.findByIsFeaturedTrueAndStatus("APPROVED")
                .stream()
                .map(this::mapToHotelDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HotelDto> getAllApprovedHotels() {
        return hotelRepository.findByStatus("APPROVED")
                .stream()
                .map(this::mapToHotelDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HotelDetailDto getHotelBySlug(String slug) {
        Hotel hotel = hotelRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with slug: " + slug));
        return mapToHotelDetailDto(hotel);
    }

    @Transactional(readOnly = true)
    public HotelDetailDto getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
        return mapToHotelDetailDto(hotel);
    }

    public HotelDto mapToHotelDto(Hotel hotel) {
        BigDecimal startingPrice = hotel.getRooms().stream()
                .filter(Room::isActive)
                .map(Room::getBasePrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .map(p -> p.setScale(2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        BigDecimal originalPrice = startingPrice;
        int discountPercentage = 0;

        List<String> images = hotel.getImages().stream()
                .map(HotelImage::getImageUrl)
                .collect(Collectors.toList());

        List<String> amenities = hotel.getAmenities().stream()
                .map(HotelAmenity::getName)
                .collect(Collectors.toList());

        Location loc = hotel.getLocation();
        boolean freeCancellation = hotel.getCancellationPolicy() != null &&
                !hotel.getCancellationPolicy().toLowerCase().contains("non-refundable");

        return HotelDto.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .slug(hotel.getSlug())
                .description(hotel.getDescription())
                .starRating(hotel.getStarRating())
                .averageRating(hotel.getAverageRating())
                .reviewCount(hotel.getReviewCount())
                .address(hotel.getAddress())
                .city(loc != null ? loc.getCity() : "")
                .state(loc != null ? loc.getState() : "")
                .country(loc != null ? loc.getCountry() : "")
                .area(loc != null ? loc.getArea() : "")
                .landmark(loc != null ? loc.getLandmark() : "")
                .latitude(loc != null ? loc.getLatitude() : null)
                .longitude(loc != null ? loc.getLongitude() : null)
                .primaryImageUrl(hotel.getPrimaryImageUrl())
                .images(images)
                .amenities(amenities)
                .startingPrice(startingPrice)
                .originalPrice(originalPrice)
                .discountPercentage(discountPercentage)
                .freeCancellation(freeCancellation)
                .cancellationPolicy(hotel.getCancellationPolicy())
                .status(hotel.getStatus())
                .isFeatured(hotel.isFeatured())
                .currency("INR")
                .build();
    }

    public HotelDetailDto mapToHotelDetailDto(Hotel hotel) {
        Location loc = hotel.getLocation();

        List<String> images = hotel.getImages().stream()
                .map(HotelImage::getImageUrl)
                .collect(Collectors.toList());

        List<String> amenities = hotel.getAmenities().stream()
                .map(HotelAmenity::getName)
                .collect(Collectors.toList());

        List<RoomDto> rooms = hotel.getRooms().stream()
                .filter(Room::isActive)
                .map(this::mapToRoomDto)
                .collect(Collectors.toList());

        BigDecimal startingPrice = rooms.stream()
                .map(RoomDto::getBasePrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .map(p -> p.setScale(2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        List<ReviewDto> reviews = reviewRepository.findByHotelIdAndStatusOrderByCreatedAtDesc(hotel.getId(), "APPROVED")
                .stream()
                .map(this::mapToReviewDto)
                .collect(Collectors.toList());

        return HotelDetailDto.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .slug(hotel.getSlug())
                .description(hotel.getDescription())
                .starRating(hotel.getStarRating())
                .averageRating(hotel.getAverageRating())
                .reviewCount(hotel.getReviewCount())
                .address(hotel.getAddress())
                .postalCode(hotel.getPostalCode())
                .city(loc != null ? loc.getCity() : "")
                .state(loc != null ? loc.getState() : "")
                .country(loc != null ? loc.getCountry() : "")
                .area(loc != null ? loc.getArea() : "")
                .landmark(loc != null ? loc.getLandmark() : "")
                .latitude(loc != null ? loc.getLatitude() : null)
                .longitude(loc != null ? loc.getLongitude() : null)
                .checkInTime(hotel.getCheckInTime())
                .checkOutTime(hotel.getCheckOutTime())
                .cancellationPolicy(hotel.getCancellationPolicy())
                .primaryImageUrl(hotel.getPrimaryImageUrl())
                .images(images)
                .amenities(amenities)
                .rooms(rooms)
                .reviews(reviews)
                .isFeatured(hotel.isFeatured())
                .startingPrice(startingPrice)
                .currency("INR")
                .build();
    }

    public RoomDto mapToRoomDto(Room room) {
        BigDecimal basePrice = room.getBasePrice() != null
                ? room.getBasePrice().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountedPrice = basePrice;
        int discountPercentage = 0;

        List<String> images = room.getImages().stream()
                .map(RoomImage::getImageUrl)
                .collect(Collectors.toList());

        List<String> amenities = room.getAmenities().stream()
                .map(RoomAmenity::getName)
                .collect(Collectors.toList());

        return RoomDto.builder()
                .id(room.getId())
                .hotelId(room.getHotel() != null ? room.getHotel().getId() : null)
                .name(room.getName())
                .roomType(room.getRoomType())
                .description(room.getDescription())
                .basePrice(basePrice)
                .discountedPrice(discountedPrice)
                .discountPercentage(discountPercentage)
                .maxAdults(room.getMaxAdults())
                .maxChildren(room.getMaxChildren())
                .bedType(room.getBedType())
                .roomSizeSqft(room.getRoomSizeSqft())
                .mealPlan(room.getMealPlan())
                .cancellationPolicy(room.getCancellationPolicy())
                .totalQuantity(room.getTotalQuantity())
                .availableQuantity(room.getTotalQuantity())
                .primaryImageUrl(room.getPrimaryImageUrl())
                .images(images)
                .amenities(amenities)
                .isActive(room.isActive())
                .currency("INR")
                .build();
    }

    public ReviewDto mapToReviewDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .hotelId(review.getHotel().getId())
                .hotelName(review.getHotel().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .userAvatar(review.getUser().getAvatarUrl())
                .overallRating(review.getOverallRating())
                .cleanlinessRating(review.getCleanlinessRating())
                .locationRating(review.getLocationRating())
                .serviceRating(review.getServiceRating())
                .facilitiesRating(review.getFacilitiesRating())
                .valueRating(review.getValueRating())
                .headline(review.getHeadline())
                .comment(review.getComment())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
