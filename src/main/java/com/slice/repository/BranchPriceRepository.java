package com.slice.repository;

import com.slice.model.BranchPrice;
import com.slice.model.enums.CarType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchPriceRepository extends JpaRepository<BranchPrice, Long> {
    Optional<BranchPrice> findByBranchIdAndCarType(Long branchId, CarType carType);
}
