package com.example.eventmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class SimpleContextTest {

    @Test
    void contextLoads() {
        // Просто проверяем что тест запускается
        assertTrue(true);
    }
}