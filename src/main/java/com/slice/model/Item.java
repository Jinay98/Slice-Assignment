package com.slice.model;

import com.slice.model.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Sample domain entity — replace with your interview entity.
 *
 * Extends {@link BaseEntity} which provides id, version (optimistic lock),
 * createdAt, and updatedAt automatically.
 *
 * INTERVIEW TIP: When designing your entity, ask yourself:
 *  1. Do concurrent writes on the same row need protection?  → @Version in BaseEntity covers it
 *  2. Do you need soft-delete?                               → use status=DELETED
 *  3. Do you need idempotency?                               → add an idempotencyKey UNIQUE column
 */
@Entity
@Table(
        name = "items",
        indexes = {
                @Index(name = "idx_items_status", columnList = "status"),
                @Index(name = "idx_items_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Item extends BaseEntity {

    @Column(nullable = false, length = 255, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ItemStatus status = ItemStatus.ACTIVE;
}
