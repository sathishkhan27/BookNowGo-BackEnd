package com.booknowgo.service;

import com.booknowgo.entity.Location;
import com.booknowgo.repository.HotelRepository;
import com.booknowgo.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final HotelRepository hotelRepository;

    @Transactional(readOnly = true)
    public List<Location> getPopularDestinations() {
        return locationRepository.findByIsPopularTrue();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPopularDestinationsWithHotelCount() {
        List<Location> popular = locationRepository.findByIsPopularTrue();
        return popular.stream().map(loc -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", loc.getId());
            map.put("city", loc.getCity());
            map.put("state", loc.getState());
            map.put("country", loc.getCountry());
            map.put("cityImage", loc.getCityImage());
            
            var hotelsInCity = hotelRepository.findByLocationId(loc.getId());
            map.put("hotelCount", hotelsInCity.size());

            BigDecimal startingPrice = hotelsInCity.stream()
                    .flatMap(h -> h.getRooms().stream())
                    .filter(r -> r.isActive() && r.getBasePrice() != null)
                    .map(r -> r.getBasePrice())
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            map.put("startingPrice", startingPrice);
            map.put("currency", "INR");
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Location> searchLocations(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return locationRepository.searchLocations(query.trim());
    }
}
