package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.repository.PassportRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("testcontainers")
class PassportRepositoryTestContainersTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private PassportRepository passportRepository;

    @BeforeEach
    void setUp() {
        if (passportRepository != null) {
            passportRepository.deleteAll();
        }
    }

    @Test
    void shouldSaveAndRetrievePassportWithRealPostgreSQL() {
        Passport passport = new Passport("1234", "567890");

        Passport saved = passportRepository.save(passport);

        assertThat(saved.getId()).isNotNull();

        Optional<Passport> found = passportRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSeries()).isEqualTo("1234");
        assertThat(found.get().getNumber()).isEqualTo("567890");
    }

    @Test
    void shouldEnforceUniqueSeriesAndNumberConstraint() {
        Passport passport1 = new Passport("1111", "222222");
        Passport passport2 = new Passport("1111", "222222");

        passportRepository.saveAndFlush(passport1);

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> passportRepository.saveAndFlush(passport2)
        )).isInstanceOf(Exception.class);
    }

    @Test
    void shouldFindBySeriesAndNumber() {
        Passport passport = new Passport("1234", "567890");
        passportRepository.save(passport);

        Optional<Passport> found = passportRepository.findBySeriesAndNumber("1234", "567890");

        assertThat(found).isPresent();
        assertThat(found.get().getSeries()).isEqualTo("1234");
        assertThat(found.get().getNumber()).isEqualTo("567890");
    }
}