package com.slice.service;

import com.slice.dto.request.CreateBookingRequest;
import com.slice.dto.response.BookingResponse;
import com.slice.exception.DuplicateResourceException;
import com.slice.model.Booking;
import com.slice.model.Branch;
import com.slice.model.BranchPrice;
import com.slice.model.Car;
import com.slice.model.enums.BookingStatus;
import com.slice.model.enums.CarType;
import com.slice.repository.BookingRepository;
import com.slice.repository.BranchPriceRepository;
import com.slice.service.impl.BookingServiceImpl;
import com.slice.strategy.CheapestFirstAllocationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BranchPriceRepository branchPriceRepository;

    @Mock
    private CheapestFirstAllocationStrategy allocationStrategy;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Branch branch;
    private Car car;
    private LocalDateTime start;

    @BeforeEach
    void setUp() {
        branch = Branch.builder().name("Central").build();
        branch.setId(10L);
        car = Car.builder()
                .branch(branch)
                .carNumber("CAR-001")
                .carType(CarType.SEDAN)
                .build();
        car.setId(20L);
        start = LocalDateTime.now().plusDays(1).withNano(0);
    }

    @Test
    void shouldRoundSixtyOneMinutesUpToNinetyMinutes() {
        CreateBookingRequest request = request(start.plusMinutes(61), "key-1");
        BranchPrice price = BranchPrice.builder()
                .branch(branch)
                .carType(CarType.SEDAN)
                .pricePerHour(new BigDecimal("100.00"))
                .build();

        when(bookingRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(allocationStrategy.process(any())).thenReturn(car);
        when(branchPriceRepository.findByBranchIdAndCarType(10L, CarType.SEDAN))
                .thenReturn(Optional.of(price));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(30L);
            return booking;
        });

        BookingResponse response = bookingService.createBooking(request);

        assertThat(response.getTotalPrice()).isEqualByComparingTo("150.00");
        assertThat(response.getPricePerHour()).isEqualByComparingTo("100.00");
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(response.getCarNumber()).isEqualTo("CAR-001");
    }

    @Test
    void shouldReturnOriginalBookingForIdenticalIdempotentRetry() {
        CreateBookingRequest request = request(start.plusHours(1), "same-key");
        Booking existing = existingBooking(request);
        when(bookingRepository.findByIdempotencyKey("same-key")).thenReturn(Optional.of(existing));

        BookingResponse response = bookingService.createBooking(request);

        assertThat(response.getBookingId()).isEqualTo(30L);
        verify(allocationStrategy, never()).process(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldRejectIdempotencyKeyReusedForDifferentRequest() {
        CreateBookingRequest request = request(start.plusHours(1), "same-key");
        Booking existing = existingBooking(request);
        when(bookingRepository.findByIdempotencyKey("same-key")).thenReturn(Optional.of(existing));

        request.setUserId(999L);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("different booking request");
    }

    @Test
    void shouldRejectEndTimeNotAfterStartTime() {
        CreateBookingRequest request = request(start, "invalid-key");

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be after");
    }

    private CreateBookingRequest request(LocalDateTime end, String key) {
        return CreateBookingRequest.builder()
                .userId(101L)
                .carType(CarType.SEDAN)
                .startDateTime(start)
                .endDateTime(end)
                .idempotencyKey(key)
                .build();
    }

    private Booking existingBooking(CreateBookingRequest request) {
        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .car(car)
                .branchId(branch.getId())
                .carType(request.getCarType())
                .startTime(request.getStartDateTime())
                .endTime(request.getEndDateTime())
                .pricePerHour(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("100.00"))
                .status(BookingStatus.CONFIRMED)
                .idempotencyKey(request.getIdempotencyKey())
                .build();
        booking.setId(30L);
        return booking;
    }
}
