package com.booknowgo.repository;

import com.booknowgo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByHotelIdAndStatusOrderByCreatedAtDesc(Long hotelId, String status);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Review> findByStatus(String status);

    @Query("SELECT AVG(r.overallRating) FROM Review r WHERE r.hotel.id = :hotelId AND r.status = 'APPROVED'")
    BigDecimal calculateAverageRatingForHotel(@Param("hotelId") Long hotelId);

    long countByHotelIdAndStatus(Long hotelId, String status);
}
