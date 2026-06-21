package com.slice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Application startup hooks.
 *
 * Singleton pattern: Spring manages all @Component beans as singletons by default.
 * The HikariCP DataSource is also a singleton — created once, shared across threads.
 * Pool size is controlled via application.properties (hikari.maximum-pool-size etc.).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppConfig {

    private final DataSource dataSource;

    /**
     * Verify DB connectivity after the application context is fully initialised.
     * Logs the connection URL so misconfigured environments are caught immediately.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try (Connection conn = dataSource.getConnection()) {
            log.info("Database connected — url={} catalog={}",
                    conn.getMetaData().getURL(),
                    conn.getCatalog());
        } catch (SQLException e) {
            log.error("Database connection failed: {}", e.getMessage());
        }
    }
}
