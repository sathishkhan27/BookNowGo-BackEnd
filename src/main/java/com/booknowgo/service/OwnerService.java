package com.booknowgo.service;

import com.booknowgo.dto.*;
import com.booknowgo.entity.*;
import com.booknowgo.exception.BadRequestException;
import com.booknowgo.exception.ResourceNotFoundException;
import com.booknowgo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomInventoryRepository roomInventoryRepository;
    private final BookingRepository bookingRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final HotelService hotelService;
    private final BookingService bookingService;

    @Transactional(readOnly = true)
    public OwnerStatsDto getOwnerStats(Long ownerId) {
        List<Booking> bookings = bookingRepository.findByHotelOwnerId(ownerId);
        long totalBookings = bookings.size();

        LocalDate today = LocalDate.now();
        long todayBookings = bookings.stream()
                .filter(b -> b.getCreatedAt().toLocalDate().equals(today))
                .count();

        long upcomingBookings = bookings.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getBookingStatus()) && b.getCheckInDate().isAfter(today.minusDays(1)))
                .count();

        long cancellationCount = bookings.stream()
                .filter(b -> "CANCELLED".equalsIgnoreCase(b.getBookingStatus()))
                .count();

        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> !"CANCELLED".equalsIgnoreCase(b.getBookingStatus()))
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Hotel> hotels = hotelRepository.findByOwnerId(ownerId);
        double avgRating = hotels.stream()
                .map(Hotel::getAverageRating)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        // Dynamic monthly revenue aggregation for last 6 months
        List<Map<String, Object>> monthly = new ArrayList<>();
        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        java.time.format.DateTimeFormatter monthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

        for (int i = 5; i >= 0; i--) {
            java.time.YearMonth targetMonth = currentMonth.minusMonths(i);
            BigDecimal monthRev = bookings.stream()
                    .filter(b -> !"CANCELLED".equalsIgnoreCase(b.getBookingStatus()) &&
                            b.getCreatedAt() != null &&
                            java.time.YearMonth.from(b.getCreatedAt()).equals(targetMonth))
                    .map(Booking::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> m = new HashMap<>();
            m.put("month", targetMonth.format(monthFormatter));
            m.put("revenue", monthRev.intValue());
            monthly.add(m);
        }

        // Dynamic occupancy rate based on current date
        int totalRoomsCapacity = hotels.stream()
                .flatMap(h -> h.getRooms().stream())
                .filter(Room::isActive)
                .mapToInt(r -> r.getTotalQuantity() > 0 ? r.getTotalQuantity() : 1)
                .sum();

        long roomsOccupiedToday = bookings.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getBookingStatus()))
                .filter(b -> !today.isBefore(b.getCheckInDate()) && today.isBefore(b.getCheckOutDate()))
                .mapToInt(Booking::getNumberOfRooms)
                .sum();

        double occupancyRate = totalRoomsCapacity > 0
                ? Math.min(100.0, Math.round(((double) roomsOccupiedToday / totalRoomsCapacity) * 1000.0) / 10.0)
                : 0.0;

        List<BookingResponse> recent = bookings.stream()
                .limit(5)
                .map(bookingService::mapToBookingResponse)
                .collect(Collectors.toList());

        return OwnerStatsDto.builder()
                .totalBookings(totalBookings)
                .todayBookings(todayBookings)
                .upcomingBookings(upcomingBookings)
                .totalRevenue(totalRevenue)
                .occupancyRate(occupancyRate)
                .cancellationCount(cancellationCount)
                .averageRating(avgRating)
                .monthlyRevenue(monthly)
                .recentBookings(recent)
                .build();
    }

    @Transactional(readOnly = true)
    public List<HotelDto> getOwnerHotels(Long ownerId) {
        return hotelRepository.findByOwnerId(ownerId)
                .stream()
                .map(hotelService::mapToHotelDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public HotelDto createHotel(Long ownerId, HotelCreateUpdateRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        String slug = request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-") + "-" + System.currentTimeMillis() % 10000;

        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .starRating(request.getStarRating() != null ? request.getStarRating() : 0)
                .averageRating(BigDecimal.ZERO)
                .reviewCount(0)
                .address(request.getAddress())
                .postalCode(request.getPostalCode())
                .location(location)
                .owner(owner)
                .status("APPROVED") // Auto-approve for owner convenience
                .checkInTime(request.getCheckInTime() != null ? request.getCheckInTime() : "14:00")
                .checkOutTime(request.getCheckOutTime() != null ? request.getCheckOutTime() : "11:00")
                .cancellationPolicy(request.getCancellationPolicy())
                .build();

        hotel = hotelRepository.save(hotel);

        if (request.getImageUrls() != null) {
            int order = 0;
            for (String url : request.getImageUrls()) {
                HotelImage img = HotelImage.builder()
                        .hotel(hotel)
                        .imageUrl(url)
                        .isPrimary(order == 0)
                        .displayOrder(order++)
                        .build();
                hotel.getImages().add(img);
            }
        }

        if (request.getAmenities() != null) {
            for (String am : request.getAmenities()) {
                HotelAmenity amenity = HotelAmenity.builder()
                        .hotel(hotel)
                        .name(am)
                        .icon("check")
                        .build();
                hotel.getAmenities().add(amenity);
            }
        }

        hotel = hotelRepository.save(hotel);
        return hotelService.mapToHotelDto(hotel);
    }

    @Transactional
    public RoomDto createRoom(Long ownerId, Long hotelId, RoomCreateUpdateRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if (!hotel.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You are not the owner of this hotel");
        }

        Room room = Room.builder()
                .hotel(hotel)
                .name(request.getName())
                .roomType(request.getRoomType())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .maxAdults(request.getMaxAdults())
                .maxChildren(request.getMaxChildren())
                .bedType(request.getBedType())
                .roomSizeSqft(request.getRoomSizeSqft())
                .mealPlan(request.getMealPlan())
                .cancellationPolicy(request.getCancellationPolicy())
                .totalQuantity(request.getTotalQuantity())
                .isActive(true)
                .build();

        room = roomRepository.save(room);

        if (request.getImageUrls() != null) {
            for (String url : request.getImageUrls()) {
                RoomImage img = RoomImage.builder()
                        .room(room)
                        .imageUrl(url)
                        .isPrimary(room.getImages().isEmpty())
                        .build();
                room.getImages().add(img);
            }
        }

        if (request.getAmenities() != null) {
            for (String am : request.getAmenities()) {
                RoomAmenity amenity = RoomAmenity.builder()
                        .room(room)
                        .name(am)
                        .build();
                room.getAmenities().add(amenity);
            }
        }

        room = roomRepository.save(room);
        return hotelService.mapToRoomDto(room);
    }

    @Transactional
    public void updateRoomInventory(Long ownerId, Long roomId, List<RoomInventoryUpdateDto> updates) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getHotel().getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You are not the owner of this room");
        }

        for (RoomInventoryUpdateDto update : updates) {
            RoomInventory inv = roomInventoryRepository.findByRoomIdAndInventoryDate(roomId, update.getDate())
                    .orElseGet(() -> RoomInventory.builder()
                            .room(room)
                            .inventoryDate(update.getDate())
                            .availableCount(update.getAvailableCount())
                            .bookedCount(0)
                            .blockedCount(0)
                            .build());

            inv.setAvailableCount(update.getAvailableCount());
            if (update.getBlockedCount() != null) inv.setBlockedCount(update.getBlockedCount());
            if (update.getPriceModifier() != null) inv.setPriceModifier(update.getPriceModifier());
            roomInventoryRepository.save(inv);
        }
    }

    @Transactional(readOnly = true)
    public HotelDetailDto getOwnerHotelById(Long ownerId, Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if (!hotel.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You are not the owner of this hotel");
        }

        return hotelService.mapToHotelDetailDto(hotel);
    }

    @Transactional
    public HotelDetailDto updateHotel(Long ownerId, Long hotelId, HotelCreateUpdateRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if (!hotel.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You are not the owner of this hotel");
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            hotel.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            hotel.setDescription(request.getDescription());
        }
        if (request.getStarRating() != null) {
            hotel.setStarRating(request.getStarRating());
        }
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            hotel.setAddress(request.getAddress().trim());
        }
        if (request.getPostalCode() != null) {
            hotel.setPostalCode(request.getPostalCode().trim());
        }
        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
            hotel.setLocation(location);
        }
        if (request.getCheckInTime() != null) {
            hotel.setCheckInTime(request.getCheckInTime());
        }
        if (request.getCheckOutTime() != null) {
            hotel.setCheckOutTime(request.getCheckOutTime());
        }
        if (request.getCancellationPolicy() != null) {
            hotel.setCancellationPolicy(request.getCancellationPolicy());
        }

        if (request.getImageUrls() != null) {
            hotel.getImages().clear();
            int order = 0;
            for (String url : request.getImageUrls()) {
                if (url != null && !url.trim().isEmpty()) {
                    HotelImage img = HotelImage.builder()
                            .hotel(hotel)
                            .imageUrl(url.trim())
                            .isPrimary(order == 0)
                            .displayOrder(order++)
                            .build();
                    hotel.getImages().add(img);
                }
            }
        }

        if (request.getAmenities() != null) {
            hotel.getAmenities().clear();
            for (String am : request.getAmenities()) {
                if (am != null && !am.trim().isEmpty()) {
                    HotelAmenity amenity = HotelAmenity.builder()
                            .hotel(hotel)
                            .name(am.trim())
                            .icon("check")
                            .build();
                    hotel.getAmenities().add(amenity);
                }
            }
        }

        hotel = hotelRepository.save(hotel);
        return hotelService.mapToHotelDetailDto(hotel);
    }

    @Transactional
    public HotelDetailDto updateHotelImages(Long ownerId, Long hotelId, List<String> imageUrls) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if (!hotel.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You are not the owner of this hotel");
        }

        hotel.getImages().clear();
        if (imageUrls != null) {
            int order = 0;
            for (String url : imageUrls) {
                if (url != null && !url.trim().isEmpty()) {
                    HotelImage img = HotelImage.builder()
                            .hotel(hotel)
                            .imageUrl(url.trim())
                            .isPrimary(order == 0)
                            .displayOrder(order++)
                            .build();
                    hotel.getImages().add(img);
                }
            }
        }

        hotel = hotelRepository.save(hotel);
        return hotelService.mapToHotelDetailDto(hotel);
    }

    @Transactional(readOnly = true)
    public List<RoomDto> getHotelRooms(Long ownerId, Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if (!hotel.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You are not the owner of this hotel");
        }

        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(hotelService::mapToRoomDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoomDto getRoomById(Long ownerId, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getHotel().getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You are not the owner of this room");
        }

        return hotelService.mapToRoomDto(room);
    }

    @Transactional
    public RoomDto updateRoom(Long ownerId, Long roomId, RoomCreateUpdateRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getHotel().getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You are not the owner of this room");
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            room.setName(request.getName().trim());
        }
        if (request.getRoomType() != null && !request.getRoomType().trim().isEmpty()) {
            room.setRoomType(request.getRoomType().trim());
        }
        if (request.getDescription() != null) {
            room.setDescription(request.getDescription());
        }
        if (request.getBasePrice() != null) {
            room.setBasePrice(request.getBasePrice());
        }
        if (request.getMaxAdults() != null) {
            room.setMaxAdults(request.getMaxAdults());
        }
        if (request.getMaxChildren() != null) {
            room.setMaxChildren(request.getMaxChildren());
        }
        if (request.getBedType() != null) {
            room.setBedType(request.getBedType());
        }
        if (request.getRoomSizeSqft() != null) {
            room.setRoomSizeSqft(request.getRoomSizeSqft());
        }
        if (request.getMealPlan() != null) {
            room.setMealPlan(request.getMealPlan());
        }
        if (request.getCancellationPolicy() != null) {
            room.setCancellationPolicy(request.getCancellationPolicy());
        }
        if (request.getTotalQuantity() != null) {
            room.setTotalQuantity(request.getTotalQuantity());
        }

        if (request.getImageUrls() != null) {
            room.getImages().clear();
            boolean first = true;
            for (String url : request.getImageUrls()) {
                if (url != null && !url.trim().isEmpty()) {
                    RoomImage img = RoomImage.builder()
                            .room(room)
                            .imageUrl(url.trim())
                            .isPrimary(first)
                            .build();
                    room.getImages().add(img);
                    first = false;
                }
            }
        }

        if (request.getAmenities() != null) {
            room.getAmenities().clear();
            for (String am : request.getAmenities()) {
                if (am != null && !am.trim().isEmpty()) {
                    RoomAmenity amenity = RoomAmenity.builder()
                            .room(room)
                            .name(am.trim())
                            .build();
                    room.getAmenities().add(amenity);
                }
            }
        }

        room = roomRepository.save(room);
        return hotelService.mapToRoomDto(room);
    }

    @Transactional
    public RoomDto updateRoomImages(Long ownerId, Long roomId, List<String> imageUrls) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getHotel().getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You are not the owner of this room");
        }

        room.getImages().clear();
        boolean first = true;
        if (imageUrls != null) {
            for (String url : imageUrls) {
                if (url != null && !url.trim().isEmpty()) {
                    RoomImage img = RoomImage.builder()
                            .room(room)
                            .imageUrl(url.trim())
                            .isPrimary(first)
                            .build();
                    room.getImages().add(img);
                    first = false;
                }
            }
        }

        room = roomRepository.save(room);
        return hotelService.mapToRoomDto(room);
    }

    @Transactional
    public void deleteRoom(Long ownerId, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getHotel().getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You are not the owner of this room");
        }

        room.setActive(false);
        roomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getOwnerBookings(Long ownerId) {
        return bookingRepository.findByHotelOwnerId(ownerId)
                .stream()
                .map(bookingService::mapToBookingResponse)
                .collect(Collectors.toList());
    }
}
