package com.slice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slice.dto.request.AddCarRequest;
import com.slice.dto.request.CreateBookingRequest;
import com.slice.dto.request.CreateBranchRequest;
import com.slice.dto.request.SetBranchPriceRequest;
import com.slice.dto.response.BookingResponse;
import com.slice.dto.response.BranchPriceResponse;
import com.slice.dto.response.BranchResponse;
import com.slice.dto.response.CarResponse;
import com.slice.exception.GlobalExceptionHandler;
import com.slice.model.enums.BookingStatus;
import com.slice.model.enums.CarType;
import com.slice.service.BookingService;
import com.slice.service.BranchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        BranchController.class,
        BookingController.class,
        GlobalExceptionHandler.class
})
class CarBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BranchService branchService;

    @MockBean
    private BookingService bookingService;

    @Test
    void shouldCreateBranch() throws Exception {
        CreateBranchRequest request = CreateBranchRequest.builder().name("Central").build();
        when(branchService.createBranch(any())).thenReturn(
                BranchResponse.builder().id(1L).name("Central").build()
        );

        mockMvc.perform(post("/api/v1/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("Central"));
    }

    @Test
    void shouldSetBranchPrice() throws Exception {
        SetBranchPriceRequest request = SetBranchPriceRequest.builder()
                .carType(CarType.SUV)
                .pricePerHour(new BigDecimal("150.00"))
                .build();
        when(branchService.setPrice(eq(1L), any())).thenReturn(
                BranchPriceResponse.builder()
                        .id(2L)
                        .branchId(1L)
                        .branchName("Central")
                        .carType(CarType.SUV)
                        .pricePerHour(new BigDecimal("150.00"))
                        .build()
        );

        mockMvc.perform(put("/api/v1/branches/1/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pricePerHour").value(150.0));
    }

    @Test
    void shouldAddCar() throws Exception {
        AddCarRequest request = AddCarRequest.builder()
                .carNumber("CAR-001")
                .carType(CarType.SEDAN)
                .build();
        when(branchService.addCar(eq(1L), any())).thenReturn(
                CarResponse.builder()
                        .id(3L)
                        .branchId(1L)
                        .branchName("Central")
                        .carNumber("CAR-001")
                        .carType(CarType.SEDAN)
                        .build()
        );

        mockMvc.perform(post("/api/v1/branches/1/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.carNumber").value("CAR-001"));
    }

    @Test
    void shouldCreateBooking() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        CreateBookingRequest request = CreateBookingRequest.builder()
                .userId(101L)
                .carType(CarType.SEDAN)
                .startDateTime(start)
                .endDateTime(start.plusMinutes(61))
                .idempotencyKey("booking-key")
                .build();
        when(bookingService.createBooking(any())).thenReturn(
                BookingResponse.builder()
                        .bookingId(4L)
                        .userId(101L)
                        .carNumber("CAR-001")
                        .branchName("Central")
                        .carType(CarType.SEDAN)
                        .startDateTime(start)
                        .endDateTime(start.plusMinutes(61))
                        .pricePerHour(new BigDecimal("100.00"))
                        .totalPrice(new BigDecimal("150.00"))
                        .status(BookingStatus.CONFIRMED)
                        .build()
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bookingId").value(4))
                .andExpect(jsonPath("$.data.totalPrice").value(150.0))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void shouldRejectBookingWithoutPositiveUserId() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        CreateBookingRequest request = CreateBookingRequest.builder()
                .userId(0L)
                .carType(CarType.SEDAN)
                .startDateTime(start)
                .endDateTime(start.plusHours(1))
                .idempotencyKey("booking-key")
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.userId").exists());
    }
}
