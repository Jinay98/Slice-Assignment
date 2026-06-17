package com.slice.repository;

import com.slice.model.Item;
import com.slice.model.enums.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for Item.
 *
 * Spring generates the implementation at runtime — no boilerplate needed.
 *
 * Naming convention:
 *  findBy<Field>         → SELECT ... WHERE field = ?
 *  existsBy<Field>       → SELECT COUNT ... WHERE field = ? > 0
 *  countBy<Field>        → SELECT COUNT ... WHERE field = ?
 *  deleteBy<Field>       → DELETE ... WHERE field = ?
 *  findBy<Field>OrderBy<Field>Asc → adds ORDER BY
 *
 * INTERVIEW TIP: Add custom queries with @Query for complex lookups.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /** Check for name uniqueness before insert */
    boolean existsByName(String name);

    /** Lookup by exact name */
    Optional<Item> findByName(String name);

    /** Paginated listing filtered by status */
    Page<Item> findByStatus(ItemStatus status, Pageable pageable);

    /** Soft-delete via status update — avoids physical row deletion */
    @Modifying
    @Query("UPDATE Item i SET i.status = :status WHERE i.id = :id")
    int updateStatusById(@Param("id") Long id, @Param("status") ItemStatus status);
}
