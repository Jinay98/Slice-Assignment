package com.slice.model;

import com.slice.model.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SAMPLE DOMAIN ENTITY — delete and replace with your interview entity.
 *
 * Key patterns demonstrated:
 *  - @Version          : Optimistic locking (prevents lost-update in concurrent writes)
 *  - @CreationTimestamp: Auto-set on first INSERT
 *  - @UpdateTimestamp  : Auto-set on every UPDATE
 *  - @Builder          : Immutable construction pattern
 *  - @Enumerated       : Stores enum as VARCHAR (readable in DB)
 *
 * INTERVIEW TIP — When designing your entity, ask yourself:
 *  1. Do concurrent writes on the same row need optimistic locking?  → keep @Version
 *  2. Do you need soft-delete?                                        → use status=DELETED
 *  3. Do you need idempotency?                                        → add an idempotencyKey UNIQUE column
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
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    /**
     * Optimistic locking — prevents concurrent overwrites.
     * JPA auto-increments this on every update.
     * If two threads read version=1, both try to update,
     * only the first succeeds; the second gets OptimisticLockException.
     */
    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
