package com.slice.dto.response;

import com.slice.model.enums.CarType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarResponse {
    private Long id;
    private Long branchId;
    private String branchName;
    private String carNumber;
    private CarType carType;
}
