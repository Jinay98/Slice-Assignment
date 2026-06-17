package com.slice.service.impl;

import com.slice.dto.request.CreateItemRequest;
import com.slice.dto.request.UpdateItemRequest;
import com.slice.dto.response.ItemResponse;
import com.slice.exception.DuplicateResourceException;
import com.slice.exception.ResourceNotFoundException;
import com.slice.model.Item;
import com.slice.model.enums.ItemStatus;
import com.slice.repository.ItemRepository;
import com.slice.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of ItemService.
 *
 * ── Transaction Strategy ────────────────────────────────────────────────
 *  @Transactional        → class-level default (read + write)
 *  @Transactional(readOnly = true) → optimises SELECT queries
 *    (signals to Hibernate: don't track dirty state, don't flush on commit)
 *
 * ── Concurrency ─────────────────────────────────────────────────────────
 *  The Item entity has @Version — if two threads read the same row and
 *  both try to update, JPA throws OptimisticLockException for the loser.
 *  Callers should catch this and retry or surface a 409 response.
 *
 * ── Mapping ─────────────────────────────────────────────────────────────
 *  toResponse() converts Entity → DTO. In a larger project, use MapStruct.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    // ── CREATE ──────────────────────────────────────────────────────────

    @Override
    public ItemResponse create(CreateItemRequest request) {
        log.info("Creating item with name='{}'", request.getName());

        if (itemRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Item", "name", request.getName());
        }

        Item item = Item.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .status(ItemStatus.ACTIVE)
                .build();

        Item saved = itemRepository.save(item);
        log.info("Item created — id={}", saved.getId());
        return toResponse(saved);
    }

    // ── READ ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ItemResponse> getAll(Pageable pageable) {
        log.debug("Fetching items page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return itemRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getById(Long id) {
        log.debug("Fetching item id={}", id);
        Item item = findItemOrThrow(id);
        return toResponse(item);
    }

    // ── UPDATE ──────────────────────────────────────────────────────────

    @Override
    public ItemResponse update(Long id, UpdateItemRequest request) {
        log.info("Updating item id={}", id);
        Item item = findItemOrThrow(id);

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());

        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }

        Item updated = itemRepository.save(item);
        log.info("Item updated — id={} version={}", updated.getId(), updated.getVersion());
        return toResponse(updated);
    }

    // ── DELETE ──────────────────────────────────────────────────────────

    @Override
    public void delete(Long id) {
        log.info("Deleting item id={}", id);
        Item item = findItemOrThrow(id);
        itemRepository.delete(item);
        log.info("Item deleted — id={}", id);
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private Item findItemOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
    }

    /**
     * Maps a JPA entity to an API response DTO.
     * INTERVIEW TIP: In production, replace manual mapping with MapStruct.
     */
    private ItemResponse toResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
