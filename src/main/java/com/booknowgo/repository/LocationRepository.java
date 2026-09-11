package com.booknowgo.repository;

import com.booknowgo.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByIsPopularTrue();

    @Query("SELECT l FROM Location l WHERE LOWER(l.city) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(l.area) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(l.landmark) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(l.country) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Location> searchLocations(@Param("query") String query);

    boolean existsByCityIgnoreCase(String city);
}
