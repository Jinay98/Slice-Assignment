package com.slice.strategy;

import com.slice.exception.BusinessException;
import com.slice.model.Car;
import com.slice.model.enums.BookingStatus;
import com.slice.repository.BookingRepository;
import com.slice.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheapestFirstAllocationStrategy implements ProcessingStrategy<AllocationRequest, Car> {

    public static final String STRATEGY_NAME = "CHEAPEST_FIRST";

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public Car process(AllocationRequest request) {
        for (Car candidate : carRepository.findEligibleCarsOrderedByPrice(request.getCarType())) {
            Car lockedCar = carRepository.findByIdForUpdate(candidate.getId()).orElse(null);
            if (lockedCar == null) {
                continue;
            }

            boolean unavailable = bookingRepository.existsOverlappingBooking(
                    lockedCar.getId(),
                    BookingStatus.CONFIRMED,
                    request.getStartDateTime(),
                    request.getEndDateTime()
            );
            if (!unavailable) {
                log.info("Allocated car={} branch={}", lockedCar.getCarNumber(), lockedCar.getBranch().getName());
                return lockedCar;
            }
        }

        throw new BusinessException(
                "No car is available for type " + request.getCarType() + " in the requested slot"
        );
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
}
