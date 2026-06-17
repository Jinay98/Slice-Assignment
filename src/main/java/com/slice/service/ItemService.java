package com.slice.service;

import com.slice.dto.request.CreateItemRequest;
import com.slice.dto.request.UpdateItemRequest;
import com.slice.dto.response.ItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Item operations.
 *
 * SOLID — Dependency Inversion Principle:
 *  Controllers depend on this INTERFACE, not the concrete implementation.
 *  This makes it trivial to swap implementations or mock in tests.
 *
 * INTERVIEW TIP: Always write the interface first, then the implementation.
 * It forces you to think about the contract before the algorithm.
 */
public interface ItemService {

    /**
     * Create a new Item.
     *
     * @param request validated creation request
     * @return created item data
     * @throws com.slice.exception.DuplicateResourceException if name already exists
     */
    ItemResponse create(CreateItemRequest request);

    /**
     * Retrieve a paginated list of all active items.
     *
     * @param pageable pagination and sort configuration
     * @return page of item responses
     */
    Page<ItemResponse> getAll(Pageable pageable);

    /**
     * Retrieve a single item by its ID.
     *
     * @param id item identifier
     * @return item data
     * @throws com.slice.exception.ResourceNotFoundException if not found
     */
    ItemResponse getById(Long id);

    /**
     * Update an existing item.
     *
     * @param id      item identifier
     * @param request update payload
     * @return updated item data
     * @throws com.slice.exception.ResourceNotFoundException if not found
     */
    ItemResponse update(Long id, UpdateItemRequest request);

    /**
     * Delete an item by ID.
     *
     * @param id item identifier
     * @throws com.slice.exception.ResourceNotFoundException if not found
     */
    void delete(Long id);
}
