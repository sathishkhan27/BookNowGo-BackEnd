package com.booknowgo.repository;

import com.booknowgo.entity.BookingGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingGuestRepository extends JpaRepository<BookingGuest, Long> {
    List<BookingGuest> findByBookingId(Long bookingId);
}
