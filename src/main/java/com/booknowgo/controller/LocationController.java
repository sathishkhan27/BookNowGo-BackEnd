package com.booknowgo.controller;

import com.booknowgo.dto.ApiResponse;
import com.booknowgo.entity.Location;
import com.booknowgo.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Destinations, cities, and landmarks")
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/popular")
    @Operation(summary = "Get popular travel destinations with starting rates")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPopularDestinations() {
        List<Map<String, Object>> destinations = locationService.getPopularDestinationsWithHotelCount();
        return ResponseEntity.ok(ApiResponse.ok(destinations, "Popular destinations retrieved"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search locations for autocomplete")
    public ResponseEntity<ApiResponse<List<Location>>> searchLocations(@RequestParam(required = false) String query) {
        List<Location> locations = locationService.searchLocations(query);
        return ResponseEntity.ok(ApiResponse.ok(locations, "Locations matching query"));
    }
}
