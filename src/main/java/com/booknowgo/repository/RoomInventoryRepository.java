package com.booknowgo.repository;

import com.booknowgo.entity.RoomInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {

    Optional<RoomInventory> findByRoomIdAndInventoryDate(Long roomId, LocalDate inventoryDate);

    @Query("SELECT ri FROM RoomInventory ri WHERE ri.room.id = :roomId " +
           "AND ri.inventoryDate >= :startDate AND ri.inventoryDate <= :endDate")
    List<RoomInventory> findByRoomAndDateRange(
        @Param("roomId") Long roomId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT ri FROM RoomInventory ri WHERE ri.room.hotel.id = :hotelId " +
           "AND ri.inventoryDate >= :startDate AND ri.inventoryDate <= :endDate")
    List<RoomInventory> findByHotelAndDateRange(
        @Param("hotelId") Long hotelId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
