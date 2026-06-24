package com.slice.dto.response;

import com.slice.model.enums.CarType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchPriceResponse {
    private Long id;
    private Long branchId;
    private String branchName;
    private CarType carType;
    private BigDecimal pricePerHour;
}
