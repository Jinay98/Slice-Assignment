package com.slice.dto.response;

import com.slice.model.enums.BookingStatus;
import com.slice.model.enums.CarType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long bookingId;
    private Long userId;
    private String carNumber;
    private String branchName;
    private CarType carType;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private BigDecimal pricePerHour;
    private BigDecimal totalPrice;
    private BookingStatus status;
}
