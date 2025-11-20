package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("testcontainers")
class EventRepositoryTestContainersTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        if (eventRepository != null) {
            eventRepository.deleteAll();
        }
    }

    @Test
    void shouldSaveAndRetrieveEventWithRealPostgreSQL() {
        Event event = new Event("Test Event",
                LocalDate.now().plusDays(5),100,
                BigDecimal.valueOf(1000),
                EventStatus.PLANNED,
                "Test Description");

        Event saved = eventRepository.save(event);

        assertThat(saved.getId()).isNotNull();

        Optional<Event> found = eventRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Event");
        assertThat(found.get().getStatus()).isEqualTo(EventStatus.PLANNED);
    }

    @Test
    void shouldEnforceUniqueNameAndDateConstraint() {
        Event event1 = new Event("Unique Event",
                LocalDate.now().plusDays(10),100,
                BigDecimal.valueOf(1500),
                EventStatus.PLANNED,
                "Description 1");

        Event event2 = new Event("Unique Event",
                LocalDate.now().plusDays(10),100,
                BigDecimal.valueOf(2000),
                EventStatus.ONGOING,
                "Description 2");

        eventRepository.saveAndFlush(event1);

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> eventRepository.saveAndFlush(event2)
        )).isInstanceOf(Exception.class);
    }
}