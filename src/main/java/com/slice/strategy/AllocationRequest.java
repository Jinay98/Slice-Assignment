package com.slice.strategy;

import com.slice.model.enums.CarType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AllocationRequest {
    private final CarType carType;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
}
