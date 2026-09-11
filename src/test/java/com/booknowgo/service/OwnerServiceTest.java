package com.booknowgo.service;

import com.booknowgo.dto.*;
import com.booknowgo.entity.*;
import com.booknowgo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private HotelService hotelService;

    @InjectMocks
    private OwnerService ownerService;

    @Test
    void testCreateRoomWithDynamicImagesAndInrPrice() {
        User owner = User.builder().id(1L).build();
        Hotel hotel = Hotel.builder().id(10L).owner(owner).rooms(new ArrayList<>()).build();

        when(hotelRepository.findById(10L)).thenReturn(Optional.of(hotel));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room r = invocation.getArgument(0);
            if (r.getId() == null) r.setId(101L);
            return r;
        });

        RoomDto expectedDto = RoomDto.builder()
                .id(101L)
                .hotelId(10L)
                .name("Presidential Beachfront Suite")
                .basePrice(BigDecimal.valueOf(18000.00).setScale(2, java.math.RoundingMode.HALF_UP))
                .currency("INR")
                .images(List.of("https://owner-cdn.com/suite-1.jpg"))
                .build();
        when(hotelService.mapToRoomDto(any(Room.class))).thenReturn(expectedDto);

        RoomCreateUpdateRequest request = RoomCreateUpdateRequest.builder()
                .name("Presidential Beachfront Suite")
                .roomType("SUITE")
                .basePrice(BigDecimal.valueOf(18000.00))
                .imageUrls(List.of("https://owner-cdn.com/suite-1.jpg"))
                .amenities(List.of("Sea View", "Jacuzzi"))
                .build();

        RoomDto created = ownerService.createRoom(1L, 10L, request);

        assertNotNull(created);
        assertEquals("INR", created.getCurrency());
        assertEquals(new BigDecimal("18000.00"), created.getBasePrice());
        assertEquals(1, created.getImages().size());
        assertEquals("https://owner-cdn.com/suite-1.jpg", created.getImages().get(0));
    }

    @Test
    void testUpdateHotelImagesDynamically() {
        User owner = User.builder().id(1L).build();
        Hotel hotel = Hotel.builder()
                .id(10L)
                .owner(owner)
                .images(new ArrayList<>())
                .amenities(new ArrayList<>())
                .rooms(new ArrayList<>())
                .build();

        when(hotelRepository.findById(10L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HotelDetailDto expectedDto = HotelDetailDto.builder()
                .id(10L)
                .primaryImageUrl("https://owner-cdn.com/new-hotel-1.jpg")
                .images(List.of("https://owner-cdn.com/new-hotel-1.jpg", "https://owner-cdn.com/new-hotel-2.jpg"))
                .currency("INR")
                .build();
        when(hotelService.mapToHotelDetailDto(any(Hotel.class))).thenReturn(expectedDto);

        List<String> newImages = List.of("https://owner-cdn.com/new-hotel-1.jpg", "https://owner-cdn.com/new-hotel-2.jpg");
        HotelDetailDto updated = ownerService.updateHotelImages(1L, 10L, newImages);

        assertNotNull(updated);
        assertEquals("INR", updated.getCurrency());
        assertEquals("https://owner-cdn.com/new-hotel-1.jpg", updated.getPrimaryImageUrl());
        assertEquals(2, updated.getImages().size());
    }
}
