package com.booknowgo.repository;

import com.booknowgo.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByBookingId(Long bookingId);
    List<Refund> findByRefundStatus(String refundStatus);
}
