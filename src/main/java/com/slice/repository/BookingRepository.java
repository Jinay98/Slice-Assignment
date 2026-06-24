package com.slice.repository;

import com.slice.model.Booking;
import com.slice.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM Booking b
            WHERE b.car.id = :carId
              AND b.status = :status
              AND b.startTime < :endTime
              AND b.endTime > :startTime
            """)
    boolean existsOverlappingBooking(
            @Param("carId") Long carId,
            @Param("status") BookingStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
