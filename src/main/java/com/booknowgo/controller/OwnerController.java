package com.booknowgo.controller;

import com.booknowgo.dto.*;
import com.booknowgo.security.UserPrincipal;
import com.booknowgo.service.OwnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HOTEL_OWNER', 'ADMIN')")
@Tag(name = "Hotel Owner Portal", description = "Operations for hotel managers and owners")
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping("/stats")
    @Operation(summary = "Get hotel owner dashboard metrics, occupancy, and revenue")
    public ResponseEntity<ApiResponse<OwnerStatsDto>> getOwnerStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        OwnerStatsDto stats = ownerService.getOwnerStats(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(stats, "Owner stats retrieved"));
    }

    @GetMapping("/hotels")
    @Operation(summary = "Get all hotels managed by this owner")
    public ResponseEntity<ApiResponse<List<HotelDto>>> getOwnerHotels(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<HotelDto> hotels = ownerService.getOwnerHotels(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(hotels, "Owner hotels retrieved"));
    }

    @PostMapping("/hotels")
    @Operation(summary = "Register or create a new hotel listing")
    public ResponseEntity<ApiResponse<HotelDto>> createHotel(
            @Valid @RequestBody HotelCreateUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        HotelDto hotel = ownerService.createHotel(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(hotel, "Hotel registered successfully"));
    }

    @GetMapping("/hotels/{hotelId}")
    @Operation(summary = "Get full details of a managed hotel including dynamic images and rooms")
    public ResponseEntity<ApiResponse<HotelDetailDto>> getOwnerHotelById(
            @PathVariable Long hotelId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        HotelDetailDto hotel = ownerService.getOwnerHotelById(userPrincipal.getId(), hotelId);
        return ResponseEntity.ok(ApiResponse.ok(hotel, "Hotel details retrieved"));
    }

    @PutMapping("/hotels/{hotelId}")
    @Operation(summary = "Update hotel listing details, images, and amenities dynamically")
    public ResponseEntity<ApiResponse<HotelDetailDto>> updateHotel(
            @PathVariable Long hotelId,
            @Valid @RequestBody HotelCreateUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        HotelDetailDto hotel = ownerService.updateHotel(userPrincipal.getId(), hotelId, request);
        return ResponseEntity.ok(ApiResponse.ok(hotel, "Hotel updated successfully"));
    }

    @PutMapping("/hotels/{hotelId}/images")
    @Operation(summary = "Update hotel image gallery dynamically")
    public ResponseEntity<ApiResponse<HotelDetailDto>> updateHotelImages(
            @PathVariable Long hotelId,
            @RequestBody List<String> imageUrls,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        HotelDetailDto hotel = ownerService.updateHotelImages(userPrincipal.getId(), hotelId, imageUrls);
        return ResponseEntity.ok(ApiResponse.ok(hotel, "Hotel images updated successfully"));
    }

    @GetMapping("/hotels/{hotelId}/rooms")
    @Operation(summary = "Get all rooms for a managed hotel")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getHotelRooms(
            @PathVariable Long hotelId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<RoomDto> rooms = ownerService.getHotelRooms(userPrincipal.getId(), hotelId);
        return ResponseEntity.ok(ApiResponse.ok(rooms, "Hotel rooms retrieved"));
    }

    @PostMapping("/hotels/{hotelId}/rooms")
    @Operation(summary = "Add a new room type to a managed hotel")
    public ResponseEntity<ApiResponse<RoomDto>> createRoom(
            @PathVariable Long hotelId,
            @Valid @RequestBody RoomCreateUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        RoomDto room = ownerService.createRoom(userPrincipal.getId(), hotelId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(room, "Room created successfully"));
    }

    @GetMapping("/rooms/{roomId}")
    @Operation(summary = "Get room details by ID")
    public ResponseEntity<ApiResponse<RoomDto>> getRoomById(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        RoomDto room = ownerService.getRoomById(userPrincipal.getId(), roomId);
        return ResponseEntity.ok(ApiResponse.ok(room, "Room retrieved successfully"));
    }

    @PutMapping("/rooms/{roomId}")
    @Operation(summary = "Update room details, INR pricing, images, and amenities dynamically")
    public ResponseEntity<ApiResponse<RoomDto>> updateRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody RoomCreateUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        RoomDto room = ownerService.updateRoom(userPrincipal.getId(), roomId, request);
        return ResponseEntity.ok(ApiResponse.ok(room, "Room updated successfully"));
    }

    @PutMapping("/rooms/{roomId}/images")
    @Operation(summary = "Update room images dynamically")
    public ResponseEntity<ApiResponse<RoomDto>> updateRoomImages(
            @PathVariable Long roomId,
            @RequestBody List<String> imageUrls,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        RoomDto room = ownerService.updateRoomImages(userPrincipal.getId(), roomId, imageUrls);
        return ResponseEntity.ok(ApiResponse.ok(room, "Room images updated successfully"));
    }

    @DeleteMapping("/rooms/{roomId}")
    @Operation(summary = "Deactivate or remove a room")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        ownerService.deleteRoom(userPrincipal.getId(), roomId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Room deactivated successfully"));
    }

    @PostMapping("/rooms/{roomId}/inventory")
    @Operation(summary = "Batch update room inventory calendar and price modifiers")
    public ResponseEntity<ApiResponse<Void>> updateRoomInventory(
            @PathVariable Long roomId,
            @Valid @RequestBody List<RoomInventoryUpdateDto> updates,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        ownerService.updateRoomInventory(userPrincipal.getId(), roomId, updates);
        return ResponseEntity.ok(ApiResponse.ok(null, "Room inventory updated"));
    }

    @GetMapping("/bookings")
    @Operation(summary = "Get customer reservations for owner's properties")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getOwnerBookings(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<BookingResponse> bookings = ownerService.getOwnerBookings(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(bookings, "Owner bookings retrieved"));
    }
}
