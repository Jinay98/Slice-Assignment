package com.slice.controller;

import com.slice.dto.request.CreateItemRequest;
import com.slice.dto.request.UpdateItemRequest;
import com.slice.dto.response.ApiResponse;
import com.slice.dto.response.ItemResponse;
import com.slice.service.ItemService;
import com.slice.util.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Item CRUD operations.
 *
 * ── API Contract ────────────────────────────────────────────────────────
 *  POST   /api/v1/items          → create item       (201 CREATED)
 *  GET    /api/v1/items          → list items paged  (200 OK)
 *  GET    /api/v1/items/{id}     → get item by id    (200 OK)
 *  PUT    /api/v1/items/{id}     → update item       (200 OK)
 *  DELETE /api/v1/items/{id}     → delete item       (200 OK)
 *
 * ── Design Notes ────────────────────────────────────────────────────────
 *  - Controller is THIN: no business logic, only HTTP concerns.
 *  - @Valid triggers Bean Validation on the request body.
 *  - Pagination is handled by Spring's Pageable (pass ?page=0&size=10&sort=createdAt,desc)
 *  - Constructor injection via @RequiredArgsConstructor (Lombok) — DIP.
 */
@RestController
@RequestMapping(AppConstants.API_V1 + "/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    // ── CREATE ──────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> create(
            @Valid @RequestBody CreateItemRequest request) {

        log.info("POST /items — name='{}'", request.getName());
        ItemResponse response = itemService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.MSG_CREATED, response));
    }

    // ── READ ────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ItemResponse>>> getAll(
            @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE,
                    sort = AppConstants.DEFAULT_SORT_FIELD) Pageable pageable) {

        Page<ItemResponse> page = itemService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getById(@PathVariable Long id) {
        log.info("GET /items/{}", id);
        ItemResponse response = itemService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── UPDATE ──────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {

        log.info("PUT /items/{}", id);
        ItemResponse response = itemService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.MSG_UPDATED, response));
    }

    // ── DELETE ──────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("DELETE /items/{}", id);
        itemService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.MSG_DELETED, null));
    }
}
