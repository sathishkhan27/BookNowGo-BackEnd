package com.booknowgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "locations", indexes = {
    @Index(name = "idx_locations_city", columnList = "city"),
    @Index(name = "idx_locations_popular", columnList = "is_popular")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(length = 100)
    private String state;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String area;

    @Column(length = 150)
    private String landmark;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(length = 500)
    private String cityImage;

    @Column(name = "is_popular")
    @Builder.Default
    private boolean isPopular = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
