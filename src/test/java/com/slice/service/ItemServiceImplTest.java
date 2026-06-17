package com.slice.service;

import com.slice.dto.request.CreateItemRequest;
import com.slice.dto.request.UpdateItemRequest;
import com.slice.dto.response.ItemResponse;
import com.slice.exception.DuplicateResourceException;
import com.slice.exception.ResourceNotFoundException;
import com.slice.model.Item;
import com.slice.model.enums.ItemStatus;
import com.slice.repository.ItemRepository;
import com.slice.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ItemServiceImpl.
 *
 * Uses Mockito to isolate the service from the real database.
 * No Spring context is loaded — these tests run FAST.
 *
 * Test structure: @Nested classes group tests by method (create/getById/update/delete).
 *
 * INTERVIEW TIP: Show this structure to demonstrate test organisation.
 * Nested tests give clear context when a specific test fails.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ItemServiceImpl Unit Tests")
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item sampleItem;
    private CreateItemRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleItem = Item.builder()
                .id(1L)
                .name("Laptop")
                .description("High-performance laptop")
                .price(BigDecimal.valueOf(1500.00))
                .status(ItemStatus.ACTIVE)
                .version(0L)
                .build();

        createRequest = CreateItemRequest.builder()
                .name("Laptop")
                .description("High-performance laptop")
                .price(BigDecimal.valueOf(1500.00))
                .build();
    }

    // ── CREATE ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create and return item when name is unique")
        void shouldCreateItemSuccessfully() {
            when(itemRepository.existsByName("Laptop")).thenReturn(false);
            when(itemRepository.save(any(Item.class))).thenReturn(sampleItem);

            ItemResponse result = itemService.create(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Laptop");
            assertThat(result.getStatus()).isEqualTo(ItemStatus.ACTIVE);

            verify(itemRepository).existsByName("Laptop");
            verify(itemRepository).save(any(Item.class));
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when name already exists")
        void shouldThrowDuplicateExceptionWhenNameExists() {
            when(itemRepository.existsByName("Laptop")).thenReturn(true);

            assertThatThrownBy(() -> itemService.create(createRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Laptop");

            verify(itemRepository, never()).save(any());
        }
    }

    // ── GET BY ID ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should return item when found")
        void shouldReturnItemWhenFound() {
            when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));

            ItemResponse result = itemService.getById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Laptop");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when id does not exist")
        void shouldThrowNotFoundWhenIdMissing() {
            when(itemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ── GET ALL ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("should return paginated items")
        void shouldReturnPageOfItems() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> itemPage = new PageImpl<>(List.of(sampleItem), pageable, 1);

            when(itemRepository.findAll(pageable)).thenReturn(itemPage);

            Page<ItemResponse> result = itemService.getAll(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Laptop");
        }

        @Test
        @DisplayName("should return empty page when no items exist")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(itemRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

            Page<ItemResponse> result = itemService.getAll(pageable);

            assertThat(result.isEmpty()).isTrue();
        }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("should update and return item when found")
        void shouldUpdateItemSuccessfully() {
            UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                    .name("Gaming Laptop")
                    .description("Updated description")
                    .price(BigDecimal.valueOf(2000.00))
                    .status(ItemStatus.ACTIVE)
                    .build();

            Item updatedItem = Item.builder()
                    .id(1L).name("Gaming Laptop")
                    .description("Updated description")
                    .price(BigDecimal.valueOf(2000.00))
                    .status(ItemStatus.ACTIVE).version(1L)
                    .build();

            when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
            when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

            ItemResponse result = itemService.update(1L, updateRequest);

            assertThat(result.getName()).isEqualTo("Gaming Laptop");
            assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(2000.00));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when item to update not found")
        void shouldThrowNotFoundWhenItemMissing() {
            when(itemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.update(99L, new UpdateItemRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should delete item when found")
        void shouldDeleteItemSuccessfully() {
            when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
            doNothing().when(itemRepository).delete(sampleItem);

            assertThatCode(() -> itemService.delete(1L)).doesNotThrowAnyException();

            verify(itemRepository).delete(sampleItem);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when item to delete not found")
        void shouldThrowNotFoundWhenItemMissing() {
            when(itemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(itemRepository, never()).delete(any());
        }
    }
}
