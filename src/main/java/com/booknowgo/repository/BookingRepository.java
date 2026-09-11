package com.booknowgo.repository;

import com.booknowgo.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Booking> findByHotelIdOrderByCreatedAtDesc(Long hotelId);

    @Query("SELECT b FROM Booking b WHERE b.hotel.owner.id = :ownerId ORDER BY b.createdAt DESC")
    List<Booking> findByHotelOwnerId(@Param("ownerId") Long ownerId);

    long countByBookingStatus(String bookingStatus);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.bookingStatus <> 'CANCELLED'")
    BigDecimal calculateTotalGrossRevenue();

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.hotel.owner.id = :ownerId AND b.bookingStatus <> 'CANCELLED'")
    BigDecimal calculateOwnerGrossRevenue(@Param("ownerId") Long ownerId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.hotel.owner.id = :ownerId")
    long countByHotelOwnerId(@Param("ownerId") Long ownerId);
}
