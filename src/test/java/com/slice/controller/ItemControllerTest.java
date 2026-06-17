package com.slice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slice.dto.request.CreateItemRequest;
import com.slice.dto.request.UpdateItemRequest;
import com.slice.dto.response.ItemResponse;
import com.slice.exception.DuplicateResourceException;
import com.slice.exception.GlobalExceptionHandler;
import com.slice.exception.ResourceNotFoundException;
import com.slice.model.enums.ItemStatus;
import com.slice.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller layer tests using @WebMvcTest.
 *
 * @WebMvcTest loads ONLY the web layer (controller + filters + exception handlers).
 * The service is mocked via @MockBean — no DB, no service logic.
 *
 * This tests:
 *  - HTTP method routing (POST/GET/PUT/DELETE)
 *  - Request body deserialization and validation
 *  - Response status codes
 *  - Response body shape (JSON path assertions)
 *  - Exception → HTTP status mapping
 *
 * INTERVIEW TIP: These run extremely fast (~200ms) because no Spring context
 * starts — perfect for demonstrating test discipline in 90 minutes.
 */
@WebMvcTest(controllers = {ItemController.class, GlobalExceptionHandler.class})
@DisplayName("ItemController Web Layer Tests")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = ItemResponse.builder()
                .id(1L)
                .name("Laptop")
                .description("High-performance laptop")
                .price(BigDecimal.valueOf(1500.00))
                .status(ItemStatus.ACTIVE)
                .build();
    }

    // ── POST /api/v1/items ────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/items")
    class CreateEndpointTests {

        @Test
        @DisplayName("should return 201 and created item on valid request")
        void shouldReturn201OnValidCreate() throws Exception {
            CreateItemRequest request = CreateItemRequest.builder()
                    .name("Laptop")
                    .description("High-performance laptop")
                    .price(BigDecimal.valueOf(1500.00))
                    .build();

            when(itemService.create(any(CreateItemRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("Laptop"));
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            CreateItemRequest request = CreateItemRequest.builder()
                    .name("")   // violates @NotBlank
                    .price(BigDecimal.valueOf(100.00))
                    .build();

            mockMvc.perform(post("/api/v1/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.data.name").exists());  // field error present
        }

        @Test
        @DisplayName("should return 409 when item name already exists")
        void shouldReturn409OnDuplicate() throws Exception {
            CreateItemRequest request = CreateItemRequest.builder()
                    .name("Laptop")
                    .price(BigDecimal.valueOf(1500.00))
                    .build();

            when(itemService.create(any())).thenThrow(new DuplicateResourceException("Item already exists"));

            mockMvc.perform(post("/api/v1/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value("ERROR"));
        }
    }

    // ── GET /api/v1/items ─────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/items")
    class GetAllEndpointTests {

        @Test
        @DisplayName("should return 200 with paginated items")
        void shouldReturn200WithItems() throws Exception {
            PageImpl<ItemResponse> page = new PageImpl<>(
                    List.of(sampleResponse), PageRequest.of(0, 10), 1);

            when(itemService.getAll(any())).thenReturn(page);

            mockMvc.perform(get("/api/v1/items"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content[0].name").value("Laptop"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }
    }

    // ── GET /api/v1/items/{id} ────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/items/{id}")
    class GetByIdEndpointTests {

        @Test
        @DisplayName("should return 200 with item when found")
        void shouldReturn200WhenFound() throws Exception {
            when(itemService.getById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/items/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("Laptop"));
        }

        @Test
        @DisplayName("should return 404 when item not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(itemService.getById(99L))
                    .thenThrow(new ResourceNotFoundException("Item not found with id: 99"));

            mockMvc.perform(get("/api/v1/items/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("Item not found with id: 99"));
        }
    }

    // ── PUT /api/v1/items/{id} ────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/v1/items/{id}")
    class UpdateEndpointTests {

        @Test
        @DisplayName("should return 200 and updated item on valid request")
        void shouldReturn200OnValidUpdate() throws Exception {
            UpdateItemRequest request = UpdateItemRequest.builder()
                    .name("Gaming Laptop")
                    .price(BigDecimal.valueOf(2000.00))
                    .build();

            ItemResponse updated = ItemResponse.builder()
                    .id(1L).name("Gaming Laptop")
                    .price(BigDecimal.valueOf(2000.00))
                    .status(ItemStatus.ACTIVE)
                    .build();

            when(itemService.update(eq(1L), any(UpdateItemRequest.class))).thenReturn(updated);

            mockMvc.perform(put("/api/v1/items/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("Gaming Laptop"));
        }
    }

    // ── DELETE /api/v1/items/{id} ─────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/v1/items/{id}")
    class DeleteEndpointTests {

        @Test
        @DisplayName("should return 200 on successful delete")
        void shouldReturn200OnDelete() throws Exception {
            doNothing().when(itemService).delete(1L);

            mockMvc.perform(delete("/api/v1/items/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"));
        }

        @Test
        @DisplayName("should return 404 when item to delete not found")
        void shouldReturn404WhenItemNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Item not found with id: 99"))
                    .when(itemService).delete(99L);

            mockMvc.perform(delete("/api/v1/items/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"));
        }
    }
}
