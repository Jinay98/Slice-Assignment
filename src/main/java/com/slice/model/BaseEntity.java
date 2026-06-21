package com.slice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Abstract base for all JPA entities.
 *
 * Centralises the fields every entity needs so concrete entities
 * only declare their own business columns.
 *
 * Extend this in every new domain entity:
 * <pre>
 *   {@literal @}Entity
 *   {@literal @}SuperBuilder
 *   {@literal @}NoArgsConstructor
 *   public class Booking extends BaseEntity { ... }
 * </pre>
 *
 * @Version   — optimistic locking; JPA increments on every UPDATE
 * @CreationTimestamp / @UpdateTimestamp — auto-managed audit fields
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
