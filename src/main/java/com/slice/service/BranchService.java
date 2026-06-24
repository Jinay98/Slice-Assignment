package com.slice.service;

import com.slice.dto.request.AddCarRequest;
import com.slice.dto.request.CreateBranchRequest;
import com.slice.dto.request.SetBranchPriceRequest;
import com.slice.dto.response.BranchPriceResponse;
import com.slice.dto.response.BranchResponse;
import com.slice.dto.response.CarResponse;

public interface BranchService {
    BranchResponse createBranch(CreateBranchRequest request);

    BranchPriceResponse setPrice(Long branchId, SetBranchPriceRequest request);

    CarResponse addCar(Long branchId, AddCarRequest request);
}
