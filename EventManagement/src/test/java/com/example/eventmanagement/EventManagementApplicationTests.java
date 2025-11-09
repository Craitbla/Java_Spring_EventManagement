package com.example.eventmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // ← ДОБАВЬТЕ ЭТУ АННОТАЦИЮ//- интеграционный тест (проверяет поднятие контекста)
class EventManagementApplicationTests {

	@Test
	void contextLoads() {
	}
}
