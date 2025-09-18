package com.example.catalog.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;

@SpringBootTest
@ActiveProfiles("test")
class ProductControllerIT {

    @Test
    void contextLoads() {
        // Integration tests with Testcontainers could go here.
    }
}
