package com.slice.service;

import com.slice.exception.BusinessException;
import com.slice.model.Branch;
import com.slice.model.Car;
import com.slice.model.enums.BookingStatus;
import com.slice.model.enums.CarType;
import com.slice.repository.BookingRepository;
import com.slice.repository.CarRepository;
import com.slice.strategy.AllocationRequest;
import com.slice.strategy.CheapestFirstAllocationStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheapestFirstAllocationStrategyTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private CheapestFirstAllocationStrategy strategy;

    @Test
    void shouldSkipUnavailableCheapestCarAndAllocateNextCandidate() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);
        Car first = car(1L, "A-001", "Alpha");
        Car second = car(2L, "B-001", "Beta");

        when(carRepository.findEligibleCarsOrderedByPrice(CarType.SEDAN))
                .thenReturn(List.of(first, second));
        when(carRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(first));
        when(carRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(second));
        when(bookingRepository.existsOverlappingBooking(
                1L, BookingStatus.CONFIRMED, start, end
        )).thenReturn(true);
        when(bookingRepository.existsOverlappingBooking(
                2L, BookingStatus.CONFIRMED, start, end
        )).thenReturn(false);

        Car allocated = strategy.process(new AllocationRequest(CarType.SEDAN, start, end));

        assertThat(allocated.getId()).isEqualTo(2L);
        verify(carRepository).findByIdForUpdate(1L);
        verify(carRepository).findByIdForUpdate(2L);
    }

    @Test
    void shouldRejectWhenNoEligibleCarExists() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);
        when(carRepository.findEligibleCarsOrderedByPrice(CarType.SUV)).thenReturn(List.of());

        assertThatThrownBy(() -> strategy.process(
                new AllocationRequest(CarType.SUV, start, end)
        )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("No car is available");
    }

    private Car car(Long id, String number, String branchName) {
        Branch branch = Branch.builder().name(branchName).build();
        branch.setId(id);
        Car car = Car.builder()
                .branch(branch)
                .carNumber(number)
                .carType(CarType.SEDAN)
                .build();
        car.setId(id);
        return car;
    }
}
