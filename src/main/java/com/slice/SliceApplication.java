package com.slice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Slice Interview Boilerplate Spring Boot application.
 *
 * ── Architecture Layers ───────────────────────────────────────────────────
 *  Controller  →  Service (interface)  →  ServiceImpl  →  Repository  →  DB
 *
 * ── Design Patterns Pre-wired ────────────────────────────────────────────
 *  • Singleton   : All Spring beans (default scope); HikariCP pool
 *  • Strategy    : ProcessingStrategy<T,R> interface (swap algorithms at runtime)
 *  • Builder     : Lombok @Builder on all DTOs and domain objects
 *  • Repository  : Spring Data JPA (data access abstraction)
 *  • Template    : Consistent CRUD pattern across ServiceImpl methods
 *
 * ── SOLID Principles ─────────────────────────────────────────────────────
 *  • SRP : One responsibility per class (controller ≠ service ≠ repo)
 *  • OCP : Add new strategies/handlers without changing existing code
 *  • LSP : ServiceImpl is drop-in replacement for Service interface
 *  • ISP : Focused, granular service interfaces
 *  • DIP : All dependencies injected via constructor (@RequiredArgsConstructor)
 *
 * ── Interview Quick-Start ────────────────────────────────────────────────
 *  1. Delete the `item` package (model/dto/service/controller/repo)
 *  2. Create your domain package with the same structure
 *  3. Update schema.sql with your tables
 *  4. Run: mvn spring-boot:run
 */
@SpringBootApplication
public class SliceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SliceApplication.class, args);
    }
}
