package com.booknowgo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnore
    private Booking booking;

    @Column(nullable = false, unique = true, length = 100)
    private String transactionReference;

    @Column(nullable = false, length = 50)
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, WALLET

    @Column(nullable = false, length = 30)
    private String paymentStatus; // PENDING, SUCCESS, FAILED, CANCELLED, REFUNDED

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    @CreationTimestamp
    private LocalDateTime paidAt;
}
