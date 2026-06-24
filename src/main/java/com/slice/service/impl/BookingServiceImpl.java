package com.slice.service.impl;

import com.slice.dto.request.CreateBookingRequest;
import com.slice.dto.response.BookingResponse;
import com.slice.exception.DuplicateResourceException;
import com.slice.exception.ResourceNotFoundException;
import com.slice.model.Booking;
import com.slice.model.BranchPrice;
import com.slice.model.Car;
import com.slice.model.enums.BookingStatus;
import com.slice.repository.BookingRepository;
import com.slice.repository.BranchPriceRepository;
import com.slice.service.BookingService;
import com.slice.strategy.AllocationRequest;
import com.slice.strategy.CheapestFirstAllocationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);
    private static final long BILLING_WINDOW_MINUTES = 30;
    private static final long BILLING_WINDOW_SECONDS = BILLING_WINDOW_MINUTES * 60;

    private final BookingRepository bookingRepository;
    private final BranchPriceRepository branchPriceRepository;
    private final CheapestFirstAllocationStrategy allocationStrategy;

    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {
        validateSlot(request);
        String idempotencyKey = request.getIdempotencyKey().trim();

        Booking existing = bookingRepository.findByIdempotencyKey(idempotencyKey)
                .orElse(null);
        if (existing != null) {
            validateIdempotentRetry(existing, request);
            return toResponse(existing);
        }

        Car car = allocationStrategy.process(new AllocationRequest(
                request.getCarType(),
                request.getStartDateTime(),
                request.getEndDateTime()
        ));
        BranchPrice branchPrice = branchPriceRepository
                .findByBranchIdAndCarType(car.getBranch().getId(), request.getCarType())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BranchPrice",
                        "branchId/carType",
                        car.getBranch().getId() + "/" + request.getCarType()
                ));

        Booking saved = bookingRepository.save(Booking.builder()
                .userId(request.getUserId())
                .car(car)
                .branchId(car.getBranch().getId())
                .carType(request.getCarType())
                .startTime(request.getStartDateTime())
                .endTime(request.getEndDateTime())
                .pricePerHour(branchPrice.getPricePerHour())
                .totalPrice(calculateTotalPrice(
                        request.getStartDateTime(),
                        request.getEndDateTime(),
                        branchPrice.getPricePerHour()
                ))
                .status(BookingStatus.CONFIRMED)
                .idempotencyKey(idempotencyKey)
                .build());

        log.info("Created booking id={} car={}", saved.getId(), car.getCarNumber());
        return toResponse(saved);
    }

    private void validateSlot(CreateBookingRequest request) {
        if (!request.getEndDateTime().isAfter(request.getStartDateTime())) {
            throw new IllegalArgumentException("End date time must be after start date time");
        }
        if (request.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date time must not be in the past");
        }
    }

    private void validateIdempotentRetry(Booking existing, CreateBookingRequest request) {
        boolean sameRequest = existing.getUserId().equals(request.getUserId())
                && existing.getCarType() == request.getCarType()
                && existing.getStartTime().equals(request.getStartDateTime())
                && existing.getEndTime().equals(request.getEndDateTime());
        if (!sameRequest) {
            throw new DuplicateResourceException(
                    "Idempotency key is already used for a different booking request"
            );
        }
    }

    private BigDecimal calculateTotalPrice(
            LocalDateTime start,
            LocalDateTime end,
            BigDecimal pricePerHour
    ) {
        long actualSeconds = Duration.between(start, end).getSeconds();
        long billableWindows = (actualSeconds + BILLING_WINDOW_SECONDS - 1) / BILLING_WINDOW_SECONDS;
        long billableMinutes = billableWindows * BILLING_WINDOW_MINUTES;

        return pricePerHour
                .multiply(BigDecimal.valueOf(billableMinutes))
                .divide(MINUTES_PER_HOUR, 2, RoundingMode.HALF_UP);
    }

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .carNumber(booking.getCar().getCarNumber())
                .branchName(booking.getCar().getBranch().getName())
                .carType(booking.getCarType())
                .startDateTime(booking.getStartTime())
                .endDateTime(booking.getEndTime())
                .pricePerHour(booking.getPricePerHour())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .build();
    }
}
