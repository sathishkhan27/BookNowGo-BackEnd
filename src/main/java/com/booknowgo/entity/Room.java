package com.booknowgo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms", indexes = {
    @Index(name = "idx_rooms_hotel", columnList = "hotel_id"),
    @Index(name = "idx_rooms_price", columnList = "base_price")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    @JsonIgnore
    private Hotel hotel;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String roomType; // DELUXE, SUITE, EXECUTIVE, STANDARD, VILLA

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Builder.Default
    private Integer maxAdults = 2;

    @Builder.Default
    private Integer maxChildren = 1;

    @Column(length = 50)
    @Builder.Default
    private String bedType = "King Bed";

    private Integer roomSizeSqft;

    @Column(length = 100)
    @Builder.Default
    private String mealPlan = "Breakfast Included";

    @Column(length = 100)
    @Builder.Default
    private String cancellationPolicy = "Free cancellation up to 24 hours before check-in";

    @Builder.Default
    private Integer totalQuantity = 5;

    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoomImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoomAmenity> amenities = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoomInventory> inventory = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    public String getPrimaryImageUrl() {
        if (images == null || images.isEmpty()) {
            return "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&auto=format&fit=crop&q=80";
        }
        return images.stream()
                .filter(RoomImage::isPrimary)
                .findFirst()
                .map(RoomImage::getImageUrl)
                .orElse(images.get(0).getImageUrl());
    }
}
