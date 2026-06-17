package com.slice.dto.response;

import com.slice.model.enums.ItemStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for an Item.
 *
 * NEVER expose your JPA entity directly in API responses:
 *  - Entities may carry Hibernate proxies that cause lazy-loading issues during serialization.
 *  - DTOs decouple your API contract from your DB schema.
 *  - Allows cherry-picking which fields to expose.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private ItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
