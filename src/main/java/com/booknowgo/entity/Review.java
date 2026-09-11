package com.booknowgo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_reviews_hotel", columnList = "hotel_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    @JsonIgnore
    private Hotel hotel;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    @JsonIgnore
    private Booking booking;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal overallRating; // 1.0 - 10.0

    @Column(precision = 3, scale = 1)
    private BigDecimal cleanlinessRating;

    @Column(precision = 3, scale = 1)
    private BigDecimal locationRating;

    @Column(precision = 3, scale = 1)
    private BigDecimal serviceRating;

    @Column(precision = 3, scale = 1)
    private BigDecimal facilitiesRating;

    @Column(precision = 3, scale = 1)
    private BigDecimal valueRating;

    @Column(length = 200)
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(length = 20)
    @Builder.Default
    private String status = "APPROVED"; // PENDING, APPROVED, REJECTED

    @CreationTimestamp
    private LocalDateTime createdAt;
}
