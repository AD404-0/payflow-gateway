package com.payflow.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic integration test to verify the Spring application context loads correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationIntegrationTest {

    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
        // It verifies that all beans can be created and wired correctly
    }
}