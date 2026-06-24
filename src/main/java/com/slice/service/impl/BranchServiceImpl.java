package com.slice.service.impl;

import com.slice.dto.request.AddCarRequest;
import com.slice.dto.request.CreateBranchRequest;
import com.slice.dto.request.SetBranchPriceRequest;
import com.slice.dto.response.BranchPriceResponse;
import com.slice.dto.response.BranchResponse;
import com.slice.dto.response.CarResponse;
import com.slice.exception.DuplicateResourceException;
import com.slice.exception.ResourceNotFoundException;
import com.slice.model.Branch;
import com.slice.model.BranchPrice;
import com.slice.model.Car;
import com.slice.repository.BranchPriceRepository;
import com.slice.repository.BranchRepository;
import com.slice.repository.CarRepository;
import com.slice.service.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchPriceRepository branchPriceRepository;
    private final CarRepository carRepository;

    @Override
    public BranchResponse createBranch(CreateBranchRequest request) {
        String name = request.getName().trim();
        if (branchRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Branch", "name", name);
        }

        Branch saved = branchRepository.save(Branch.builder().name(name).build());
        log.info("Created branch id={} name={}", saved.getId(), saved.getName());
        return toBranchResponse(saved);
    }

    @Override
    public BranchPriceResponse setPrice(Long branchId, SetBranchPriceRequest request) {
        Branch branch = findBranchOrThrow(branchId);
        BranchPrice branchPrice = branchPriceRepository
                .findByBranchIdAndCarType(branchId, request.getCarType())
                .orElseGet(() -> BranchPrice.builder()
                        .branch(branch)
                        .carType(request.getCarType())
                        .build());

        branchPrice.setPricePerHour(request.getPricePerHour());
        BranchPrice saved = branchPriceRepository.save(branchPrice);
        log.info(
                "Set price branchId={} carType={} price={}",
                branchId,
                request.getCarType(),
                request.getPricePerHour()
        );
        return toBranchPriceResponse(saved);
    }

    @Override
    public CarResponse addCar(Long branchId, AddCarRequest request) {
        Branch branch = findBranchOrThrow(branchId);
        String carNumber = request.getCarNumber().trim().toUpperCase();
        if (carRepository.existsByCarNumberIgnoreCase(carNumber)) {
            throw new DuplicateResourceException("Car", "carNumber", carNumber);
        }

        Car saved = carRepository.save(Car.builder()
                .branch(branch)
                .carNumber(carNumber)
                .carType(request.getCarType())
                .build());
        log.info("Added car id={} branchId={} number={}", saved.getId(), branchId, carNumber);
        return toCarResponse(saved);
    }

    private Branch findBranchOrThrow(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));
    }

    private BranchResponse toBranchResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .createdAt(branch.getCreatedAt())
                .build();
    }

    private BranchPriceResponse toBranchPriceResponse(BranchPrice branchPrice) {
        return BranchPriceResponse.builder()
                .id(branchPrice.getId())
                .branchId(branchPrice.getBranch().getId())
                .branchName(branchPrice.getBranch().getName())
                .carType(branchPrice.getCarType())
                .pricePerHour(branchPrice.getPricePerHour())
                .build();
    }

    private CarResponse toCarResponse(Car car) {
        return CarResponse.builder()
                .id(car.getId())
                .branchId(car.getBranch().getId())
                .branchName(car.getBranch().getName())
                .carNumber(car.getCarNumber())
                .carType(car.getCarType())
                .build();
    }
}
