package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.repository.ClientRepository;
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
@ActiveProfiles("testcontainers") // Используем отдельный профиль
class ClientRepositoryTestContainersTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Также можно переопределить другие свойства если нужно
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private ClientRepository clientRepository;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        if (clientRepository != null) {
            clientRepository.deleteAll();
        }
    }

    @Test
    void shouldSaveAndRetrieveClientWithRealPostgreSQL() {
        // Arrange
        Passport passport = new Passport("1234", "567890");
        Client client = new Client("Test User", "+79123456789", "test@mail.com", passport);

        // Act
        Client saved = clientRepository.save(client);

        // Assert
        assertThat(saved.getId()).isNotNull();

        Optional<Client> found = clientRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Test User");
        assertThat(found.get().getPassport().getSeries()).isEqualTo("1234");
    }

    @Test
    void shouldEnforceUniquePhoneNumberConstraint() {
        // Arrange
        Passport passport1 = new Passport("1111", "222222");
        Passport passport2 = new Passport("3333", "444444");

        Client client1 = new Client("User One", "+79123456789", "user1@mail.com", passport1);
        Client client2 = new Client("User Two", "+79123456789", "user2@mail.com", passport2);

        // Act & Assert
        Client savedClient1 = clientRepository.save(client1);
        assertThat(savedClient1.getId()).isNotNull();

        // Вторая запись с тем же номером телефона должна вызвать исключение
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> {
                    clientRepository.saveAndFlush(client2);
                }
        )).isInstanceOf(Exception.class);
    }
}