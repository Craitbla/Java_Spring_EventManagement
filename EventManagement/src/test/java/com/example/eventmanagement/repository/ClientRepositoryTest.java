package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;//

//# Все тесты
//./mvnw test
//
//# Только тесты репозиториев
//./mvnw test -Dtest="*RepositoryTest"
//
//# Только ClientRepositoryTest
//./mvnw test -Dtest="ClientRepositoryTest"
@DataJpaTest
class ClientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    // Простые CRUD тесты
    @Test
    void shouldSaveClient() {
        Client client = new Client("John Doe", "+79123456789", "john@test.com");
        Client saved = clientRepository.save(client);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldFindByPhoneNumber() {
        Client client = new Client("John Doe", "+79123456789", "john@test.com");
        clientRepository.save(client);

        Optional<Client> found = clientRepository.findByPhoneNumber("+79123456789");
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
    }

    // Тест для @Query метода
    @Test
    void shouldSearchClients() {
        Client client1 = new Client("John Doe", "+79123456789", "john@test.com");
        Client client2 = new Client("Jane Smith", "+79123456780", "jane@test.com");
        clientRepository.saveAll(List.of(client1, client2));

        List<Client> results = clientRepository.searchClients("John");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFullName()).isEqualTo("John Doe");
    }

    @Test
    void shouldSaveAndFindClient() {
        // Given
        Client client = new Client();
        client.setFullName("John Doe");
        client.setPhoneNumber("+79123456789");
        client.setEmail("john@test.com");

        // When
        Client saved = clientRepository.save(client);
        Optional<Client> found = clientRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
    }
}
