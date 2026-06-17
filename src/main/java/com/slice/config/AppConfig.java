package com.slice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Application-level configuration and startup hooks.
 *
 * ── Singleton Pattern ────────────────────────────────────────────────────
 * Spring manages ALL @Service, @Repository, and @Component beans as
 * Singletons by default. The HikariCP DataSource (auto-configured by
 * Spring Boot via spring.datasource.* properties) is also a Singleton —
 * created once, shared across all threads for the lifetime of the app.
 *
 * HikariCP pool size is controlled via application.properties:
 *   spring.datasource.hikari.maximum-pool-size=10
 *   spring.datasource.hikari.minimum-idle=2
 *   spring.datasource.hikari.connection-timeout=30000
 *
 * ── SOLID Summary ────────────────────────────────────────────────────────
 * SRP : Each class has ONE responsibility (controller ≠ service ≠ repo)
 * OCP : New strategies/handlers added WITHOUT modifying existing code
 * LSP : ItemServiceImpl is a drop-in replacement for ItemService
 * ISP : Service interfaces are focused and granular
 * DIP : All dependencies injected via constructor (@RequiredArgsConstructor)
 *
 * ── Design Patterns used ─────────────────────────────────────────────────
 * Singleton  → All Spring beans; HikariCP DataSource
 * Strategy   → ProcessingStrategy<T,R> interface (swap algorithms at runtime)
 * Builder    → Lombok @Builder on all DTOs and entities
 * Repository → Spring Data JPA (ItemRepository extends JpaRepository)
 */
@Component
@Slf4j
public class AppConfig {

    private final DataSource dataSource;

    public AppConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Verify DB connectivity on startup and log pool info.
     * Fires after the application context is fully initialised.
     */
    @EventListener(ApplicationReadyEvent.class)
    @ConditionalOnProperty(name = "spring.datasource.url")
    public void onApplicationReady() {
        try (Connection conn = dataSource.getConnection()) {
            log.info("✅ Database connected — url={} catalog={}",
                    conn.getMetaData().getURL(),
                    conn.getCatalog());
        } catch (SQLException e) {
            log.error("❌ Database connection failed: {}", e.getMessage());
        }
    }
}
