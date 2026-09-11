package com.booknowgo.repository;

import com.booknowgo.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    Optional<Hotel> findBySlug(String slug);

    List<Hotel> findByStatus(String status);

    List<Hotel> findByIsFeaturedTrueAndStatus(String status);

    List<Hotel> findByOwnerId(Long ownerId);

    long countByStatus(String status);

    @Query("SELECT h FROM Hotel h JOIN h.location l WHERE h.status = 'APPROVED' AND (" +
           "LOWER(h.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.area) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.landmark) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.state) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.country) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Hotel> searchByNameOrLocation(@Param("query") String query);

    @Query("SELECT h FROM Hotel h WHERE h.location.id = :locationId AND h.status = 'APPROVED'")
    List<Hotel> findByLocationId(@Param("locationId") Long locationId);
}
