package com.booknowgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotels", indexes = {
    @Index(name = "idx_hotels_location", columnList = "location_id"),
    @Index(name = "idx_hotels_status", columnList = "status"),
    @Index(name = "idx_hotels_rating", columnList = "average_rating")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer starRating;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.valueOf(0.0);

    @Builder.Default
    private Integer reviewCount = 0;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(length = 20)
    private String postalCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(length = 30)
    @Builder.Default
    private String status = "APPROVED"; // PENDING, APPROVED, REJECTED, SUSPENDED

    @Column(length = 20)
    @Builder.Default
    private String checkInTime = "14:00";

    @Column(length = 20)
    @Builder.Default
    private String checkOutTime = "11:00";

    @Column(columnDefinition = "TEXT")
    private String cancellationPolicy;

    @Builder.Default
    private boolean isFeatured = false;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<HotelImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HotelAmenity> amenities = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public String getPrimaryImageUrl() {
        if (images == null || images.isEmpty()) {
            return "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&auto=format&fit=crop&q=80";
        }
        return images.stream()
                .filter(HotelImage::isPrimary)
                .findFirst()
                .map(HotelImage::getImageUrl)
                .orElse(images.get(0).getImageUrl());
    }
}
