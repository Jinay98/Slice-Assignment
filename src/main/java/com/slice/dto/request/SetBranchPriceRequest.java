package com.slice.dto.request;

import com.slice.model.enums.CarType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetBranchPriceRequest {

    @NotNull(message = "Car type is required")
    private CarType carType;

    @NotNull(message = "Price per hour is required")
    @DecimalMin(value = "0.01", message = "Price per hour must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimals")
    private BigDecimal pricePerHour;
}
