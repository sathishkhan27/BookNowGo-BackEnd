package com.booknowgo.service;

import com.booknowgo.dto.HotelDetailDto;
import com.booknowgo.dto.HotelDto;
import com.booknowgo.dto.RoomDto;
import com.booknowgo.entity.*;
import com.booknowgo.repository.HotelRepository;
import com.booknowgo.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private HotelService hotelService;

    @Test
    void testMapToHotelDtoDynamicStartingPriceAndImages() {
        Hotel hotel = Hotel.builder()
                .id(10L)
                .name("Taj View Retreat")
                .slug("taj-view-retreat")
                .cancellationPolicy("Free cancellation up to 48 hours")
                .build();

        // Dynamic images from owner
        HotelImage img1 = HotelImage.builder().imageUrl("https://owner-hotel-img.com/hero.jpg").isPrimary(true).displayOrder(0).build();
        HotelImage img2 = HotelImage.builder().imageUrl("https://owner-hotel-img.com/pool.jpg").isPrimary(false).displayOrder(1).build();
        hotel.setImages(new ArrayList<>(List.of(img1, img2)));

        // Dynamic active rooms from owner
        Room room1 = Room.builder().id(101L).name("Standard").basePrice(BigDecimal.valueOf(4500.00)).isActive(true).hotel(hotel).build();
        Room room2 = Room.builder().id(102L).name("Deluxe").basePrice(BigDecimal.valueOf(8000.00)).isActive(true).hotel(hotel).build();
        hotel.setRooms(new ArrayList<>(List.of(room1, room2)));

        HotelDto dto = hotelService.mapToHotelDto(hotel);

        assertNotNull(dto);
        assertEquals("INR", dto.getCurrency());
        assertEquals("https://owner-hotel-img.com/hero.jpg", dto.getPrimaryImageUrl());
        assertEquals(2, dto.getImages().size());
        assertEquals(new BigDecimal("4500.00"), dto.getStartingPrice());
        assertTrue(dto.isFreeCancellation());
    }

    @Test
    void testMapToHotelDetailDtoDynamicRoomsAndImages() {
        Hotel hotel = Hotel.builder()
                .id(20L)
                .name("Goa Sands Villa")
                .slug("goa-sands-villa")
                .build();

        Room room = Room.builder()
                .id(201L)
                .name("Private Pool Villa")
                .basePrice(BigDecimal.valueOf(15000.00))
                .isActive(true)
                .hotel(hotel)
                .build();
        hotel.setRooms(new ArrayList<>(List.of(room)));

        when(reviewRepository.findByHotelIdAndStatusOrderByCreatedAtDesc(20L, "APPROVED"))
                .thenReturn(Collections.emptyList());

        HotelDetailDto detail = hotelService.mapToHotelDetailDto(hotel);

        assertNotNull(detail);
        assertEquals("INR", detail.getCurrency());
        assertEquals(new BigDecimal("15000.00"), detail.getStartingPrice());
        assertEquals(1, detail.getRooms().size());
        assertEquals("INR", detail.getRooms().get(0).getCurrency());
        assertEquals(new BigDecimal("15000.00"), detail.getRooms().get(0).getBasePrice());
    }
}
