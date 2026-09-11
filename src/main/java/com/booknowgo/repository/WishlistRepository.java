package com.booknowgo.repository;

import com.booknowgo.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Wishlist> findByUserIdAndHotelId(Long userId, Long hotelId);
    boolean existsByUserIdAndHotelId(Long userId, Long hotelId);
    void deleteByUserIdAndHotelId(Long userId, Long hotelId);
}
