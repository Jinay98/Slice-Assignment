package com.slice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring context loads cleanly.
 * Uses the 'test' profile which connects to H2 in-memory DB (no MySQL needed).
 */
@SpringBootTest
@ActiveProfiles("test")
class SliceApplicationTests {

    @Test
    void contextLoads() {
        // If this test passes, the entire Spring context (all beans) wired up correctly.
    }
}
