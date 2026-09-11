package com.booknowgo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "room_inventory", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_id", "inventory_date"})
}, indexes = {
    @Index(name = "idx_inventory_date", columnList = "inventory_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnore
    private Room room;

    @Column(nullable = false)
    private LocalDate inventoryDate;

    @Column(nullable = false)
    private Integer availableCount;

    @Builder.Default
    private Integer bookedCount = 0;

    @Builder.Default
    private Integer blockedCount = 0;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal priceModifier = BigDecimal.ZERO;
}
