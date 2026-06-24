package com.slice.controller;

import com.slice.dto.request.AddCarRequest;
import com.slice.dto.request.CreateBranchRequest;
import com.slice.dto.request.SetBranchPriceRequest;
import com.slice.dto.response.ApiResponse;
import com.slice.dto.response.BranchPriceResponse;
import com.slice.dto.response.BranchResponse;
import com.slice.dto.response.CarResponse;
import com.slice.service.BranchService;
import com.slice.util.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_V1 + "/branches")
@RequiredArgsConstructor
@Slf4j
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @Valid @RequestBody CreateBranchRequest request
    ) {
        BranchResponse response = branchService.createBranch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.MSG_CREATED, response));
    }

    @PutMapping("/{branchId}/prices")
    public ResponseEntity<ApiResponse<BranchPriceResponse>> setPrice(
            @PathVariable Long branchId,
            @Valid @RequestBody SetBranchPriceRequest request
    ) {
        BranchPriceResponse response = branchService.setPrice(branchId, request);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.MSG_UPDATED, response));
    }

    @PostMapping("/{branchId}/cars")
    public ResponseEntity<ApiResponse<CarResponse>> addCar(
            @PathVariable Long branchId,
            @Valid @RequestBody AddCarRequest request
    ) {
        CarResponse response = branchService.addCar(branchId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.MSG_CREATED, response));
    }
}
