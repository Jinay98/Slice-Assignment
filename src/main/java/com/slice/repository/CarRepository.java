package com.slice.repository;

import com.slice.model.Car;
import com.slice.model.enums.CarType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    boolean existsByCarNumberIgnoreCase(String carNumber);

    @Query("""
            SELECT c
            FROM Car c
            JOIN BranchPrice bp
              ON bp.branch = c.branch
             AND bp.carType = c.carType
            WHERE c.carType = :carType
            ORDER BY bp.pricePerHour ASC, c.branch.name ASC, c.carNumber ASC
            """)
    List<Car> findEligibleCarsOrderedByPrice(@Param("carType") CarType carType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Car c JOIN FETCH c.branch WHERE c.id = :id")
    Optional<Car> findByIdForUpdate(@Param("id") Long id);
}
