package com.booknowgo.controller;

import com.booknowgo.dto.HotelDetailDto;
import com.booknowgo.dto.RoomDto;
import com.booknowgo.security.UserPrincipal;
import com.booknowgo.service.OwnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OwnerService ownerService;

    @InjectMocks
    private OwnerController ownerController;

    @BeforeEach
    void setUp() {
        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(UserPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return UserPrincipal.builder()
                        .id(1L)
                        .email("owner@booknowgo.com")
                        .authorities(List.of(new SimpleGrantedAuthority("ROLE_HOTEL_OWNER")))
                        .build();
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(ownerController)
                .setCustomArgumentResolvers(principalResolver)
                .build();
    }

    @Test
    void testGetOwnerHotelByIdReturnsDynamicImagesAndInr() throws Exception {
        HotelDetailDto detail = HotelDetailDto.builder()
                .id(1L)
                .name("Grand Luxury Palace")
                .primaryImageUrl("https://owner-img.com/main.jpg")
                .images(List.of("https://owner-img.com/main.jpg", "https://owner-img.com/pool.jpg"))
                .currency("INR")
                .startingPrice(BigDecimal.valueOf(8500.00))
                .build();

        when(ownerService.getOwnerHotelById(eq(1L), eq(1L))).thenReturn(detail);

        mockMvc.perform(get("/api/v1/owner/hotels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Grand Luxury Palace"))
                .andExpect(jsonPath("$.data.currency").value("INR"))
                .andExpect(jsonPath("$.data.primaryImageUrl").value("https://owner-img.com/main.jpg"))
                .andExpect(jsonPath("$.data.images.length()").value(2));
    }

    @Test
    void testUpdateHotelImagesDynamically() throws Exception {
        HotelDetailDto updated = HotelDetailDto.builder()
                .id(1L)
                .name("Grand Luxury Palace")
                .primaryImageUrl("https://owner-img.com/new-view.jpg")
                .images(List.of("https://owner-img.com/new-view.jpg"))
                .currency("INR")
                .build();

        when(ownerService.updateHotelImages(eq(1L), eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/owner/hotels/1/images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"https://owner-img.com/new-view.jpg\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.primaryImageUrl").value("https://owner-img.com/new-view.jpg"));
    }

    @Test
    void testUpdateRoomDynamically() throws Exception {
        RoomDto room = RoomDto.builder()
                .id(101L)
                .name("Royal Presidential Suite")
                .basePrice(BigDecimal.valueOf(22000.00))
                .currency("INR")
                .primaryImageUrl("https://owner-img.com/room.jpg")
                .build();

        when(ownerService.updateRoom(eq(1L), eq(101L), any())).thenReturn(room);

        String payload = """
                {
                    "name": "Royal Presidential Suite",
                    "roomType": "SUITE",
                    "basePrice": 22000.00,
                    "maxAdults": 3,
                    "maxChildren": 2,
                    "totalQuantity": 4,
                    "imageUrls": ["https://owner-img.com/room.jpg"]
                }
                """;

        mockMvc.perform(put("/api/v1/owner/rooms/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currency").value("INR"))
                .andExpect(jsonPath("$.data.basePrice").value(22000.00))
                .andExpect(jsonPath("$.data.primaryImageUrl").value("https://owner-img.com/room.jpg"));
    }
}
