package com.slice.dto.request;

import com.slice.model.enums.CarType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCarRequest {

    @NotBlank(message = "Car number is required")
    @Size(max = 50, message = "Car number must not exceed 50 characters")
    private String carNumber;

    @NotNull(message = "Car type is required")
    private CarType carType;
}
