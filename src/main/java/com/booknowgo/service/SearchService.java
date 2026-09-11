package com.booknowgo.service;

import com.booknowgo.dto.HotelDto;
import com.booknowgo.dto.HotelSearchRequest;
import com.booknowgo.entity.Hotel;
import com.booknowgo.entity.HotelAmenity;
import com.booknowgo.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final HotelRepository hotelRepository;
    private final HotelService hotelService;

    @Transactional(readOnly = true)
    public List<HotelDto> searchHotels(HotelSearchRequest criteria) {
        List<Hotel> baseHotels;

        if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
            baseHotels = hotelRepository.searchByNameOrLocation(criteria.getQuery().trim());
        } else {
            baseHotels = hotelRepository.findByStatus("APPROVED");
        }

        // Convert to DTOs for calculation and filtering
        List<HotelDto> hotelDtos = baseHotels.stream()
                .filter(h -> "APPROVED".equalsIgnoreCase(h.getStatus()))
                .map(hotelService::mapToHotelDto)
                .collect(Collectors.toList());

        // Filter: Star rating
        if (criteria.getStarRating() != null && criteria.getStarRating() > 0) {
            hotelDtos = hotelDtos.stream()
                    .filter(h -> h.getStarRating() != null && h.getStarRating().equals(criteria.getStarRating()))
                    .collect(Collectors.toList());
        }

        // Filter: Minimum guest rating (e.g. 8.0+)
        if (criteria.getMinRating() != null) {
            hotelDtos = hotelDtos.stream()
                    .filter(h -> h.getAverageRating() != null && h.getAverageRating().compareTo(criteria.getMinRating()) >= 0)
                    .collect(Collectors.toList());
        }

        // Filter: Price Range
        if (criteria.getMinPrice() != null) {
            hotelDtos = hotelDtos.stream()
                    .filter(h -> h.getStartingPrice() != null && h.getStartingPrice().compareTo(criteria.getMinPrice()) >= 0)
                    .collect(Collectors.toList());
        }
        if (criteria.getMaxPrice() != null) {
            hotelDtos = hotelDtos.stream()
                    .filter(h -> h.getStartingPrice() != null && h.getStartingPrice().compareTo(criteria.getMaxPrice()) <= 0)
                    .collect(Collectors.toList());
        }

        // Filter: Free Cancellation
        if (criteria.getFreeCancellation() != null && criteria.getFreeCancellation()) {
            hotelDtos = hotelDtos.stream()
                    .filter(HotelDto::isFreeCancellation)
                    .collect(Collectors.toList());
        }

        // Filter: Amenities
        if (criteria.getAmenities() != null && !criteria.getAmenities().isEmpty()) {
            hotelDtos = hotelDtos.stream()
                    .filter(h -> h.getAmenities() != null &&
                            criteria.getAmenities().stream().allMatch(reqAmenity ->
                                    h.getAmenities().stream().anyMatch(a -> a.equalsIgnoreCase(reqAmenity))))
                    .collect(Collectors.toList());
        }

        // Sorting
        String sortBy = criteria.getSortBy() != null ? criteria.getSortBy().toLowerCase() : "recommended";
        switch (sortBy) {
            case "price_asc":
                hotelDtos.sort(Comparator.comparing(HotelDto::getStartingPrice, Comparator.nullsLast(BigDecimal::compareTo)));
                break;
            case "price_desc":
                hotelDtos.sort(Comparator.comparing(HotelDto::getStartingPrice, Comparator.nullsLast(BigDecimal::compareTo)).reversed());
                break;
            case "rating":
                hotelDtos.sort(Comparator.comparing(HotelDto::getAverageRating, Comparator.nullsLast(BigDecimal::compareTo)).reversed());
                break;
            case "stars":
                hotelDtos.sort(Comparator.comparing(HotelDto::getStarRating, Comparator.nullsLast(Integer::compareTo)).reversed());
                break;
            case "recommended":
            default:
                hotelDtos.sort((a, b) -> {
                    if (a.isFeatured() != b.isFeatured()) {
                        return a.isFeatured() ? -1 : 1;
                    }
                    return b.getAverageRating().compareTo(a.getAverageRating());
                });
                break;
        }

        return hotelDtos;
    }
}
