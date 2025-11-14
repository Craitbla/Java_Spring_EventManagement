package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    private Passport passport1;
    private Passport passport2;
    private Passport passport3;
    private Client client1;
    private Client client2;
    private Client client3;

    @BeforeEach
    void setUp() {
        // Создаем и сохраняем тестовые данные
        passport1 = new Passport("1234", "567890");
        passport2 = new Passport("5678", "901234");
        passport3 = new Passport("7237", "215376");

        entityManager.persist(passport1);
        entityManager.persist(passport2);
        entityManager.persist(passport3);
        LocalDateTime now = LocalDateTime.now();

        client1 = Client.createForTesting("Иванов Иван Иванович", "+79123456789", "ivanov@mail.com", passport1, now.minusDays(2), now.minusDays(1));
        client2 = Client.createForTesting("Петров Петр Петрович", "+79223456789", "petrov@mail.com", passport2, now.minusDays(1), now);
        client3 = Client.createForTesting("Сидорова Анна", "+79323456789", "sidorova@mail.com", passport3, now, now);

        entityManager.persist(client1);
        entityManager.persist(client2);
        entityManager.persist(client3);

        entityManager.flush();
        entityManager.clear(); // Очищаем контекст для чистоты тестов
    }

    @Test
    void shouldFindByFullNameIgnoreCase() {
        // Act
        List<Client> foundClients = clientRepository.findByFullNameIgnoreCase("иванов иван иванович");

        // Assert
        assertThat(foundClients).hasSize(1);
        assertThat(foundClients.get(0).getFullName()).isEqualTo("Иванов Иван Иванович");
    }

    @Test
    void shouldFindByFullNameContainingIgnoreCase() {
        // Act
        List<Client> foundClients = clientRepository.findByFullNameContainingIgnoreCase("иван");

        // Assert
        assertThat(foundClients).hasSize(1);
        assertThat(foundClients.get(0).getFullName()).contains("Иван");
    }

    @Test
    void shouldFindByPhoneNumber() {
        // Act
        Optional<Client> foundClient = clientRepository.findByPhoneNumber("+79123456789");

        // Assert
        assertThat(foundClient).isPresent();
        assertThat(foundClient.get().getPhoneNumber()).isEqualTo("+79123456789");
    }

    @Test
    void shouldNotFindByNonExistentPhoneNumber() {
        // Act
        Optional<Client> foundClient = clientRepository.findByPhoneNumber("+79999999999");

        // Assert
        assertThat(foundClient).isEmpty();
    }

    @Test
    void shouldFindByEmail() {
        // Act
        Optional<Client> foundClient = clientRepository.findByEmail("ivanov@mail.com");

        // Assert
        assertThat(foundClient).isPresent();
        assertThat(foundClient.get().getEmail()).isEqualTo("ivanov@mail.com");
    }

    @Test
    void shouldFindByPassport() {
        // Act
        List<Client> foundClients = clientRepository.findByPassport(passport1);

        // Assert
        assertThat(foundClients).hasSize(1);
        assertThat(foundClients.get(0).getPassport()).isEqualTo(passport1);
    }

    @Test
    void shouldFindByCreatedAtBefore() {
        // Arrange
        LocalDateTime threshold = LocalDateTime.now().minusHours(12);

        // Act
        List<Client> foundClients = clientRepository.findByCreatedAtBefore(threshold);

        // Assert
        assertThat(foundClients).hasSize(2); // client1 и client2 созданы более 12 часов назад
    }

    @Test
    void shouldFindByCreatedAtBetween() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now().minusDays(1).plusHours(1);

        // Act
        List<Client> foundClients = clientRepository.findByCreatedAtBetween(start, end);

        // Assert
        assertThat(foundClients).hasSize(2); // client1 и client2
    }

    @Test
    void shouldFindByUpdatedAtAfter() {
        // Arrange
        LocalDateTime threshold = LocalDateTime.now().minusHours(12);

        // Act
        List<Client> foundClients = clientRepository.findByUpdatedAtAfter(threshold);

        // Assert
        assertThat(foundClients).hasSize(2); // client2 и client3
    }

    @Test
    void shouldFindByIdWithPassport() {
        // Act
        Optional<Client> foundClient = clientRepository.findByIdWithPassport(client1.getId());

        // Assert
        assertThat(foundClient).isPresent();
        assertThat(foundClient.get().getPassport()).isNotNull();
        assertThat(foundClient.get().getPassport().getSeries()).isEqualTo("1234");
        // Проверяем, что паспорт загружен без LazyInitializationException
        assertThat(foundClient.get().getPassport().getClient()).isNotNull();
    }

    @Test
    void shouldFindByIdWithTicketReservations() {
        // Arrange - создаем резервацию для клиента
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
//        reservation.assignClient(client1);
        client1.addTicketReservation(reservation); //обратка тоже есть внутри

        Event event = new Event("Концерт", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        entityManager.persist(event);
//        reservation.assignEvent(event);
        event.addTicketReservation(reservation);

        entityManager.persist(reservation);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Client> foundClient = clientRepository.findByIdWithTicketReservations(client1.getId());

        // Assert
        assertThat(foundClient).isPresent();
        assertThat(foundClient.get().getTicketReservations()).hasSize(1);
        assertThat(foundClient.get().getTicketReservations().get(0).getNumberOfTickets()).isEqualTo(2);
    }

    @Test
    void shouldSearchClientsByName() {
        // Act
        List<Client> foundClients = clientRepository.searchClients("Иванов");

        // Assert
        assertThat(foundClients).hasSize(1);
        assertThat(foundClients.get(0).getFullName()).isEqualTo("Иванов Иван Иванович");
    }

    @Test
    void shouldSearchClientsByPhoneNumber() {
        // Act
        List<Client> foundClients = clientRepository.searchClients("912345");

        // Assert
        assertThat(foundClients).hasSize(1);
        assertThat(foundClients.get(0).getPhoneNumber()).isEqualTo("+79123456789");
    }

    @Test
    void shouldSearchClientsByEmail() {
        // Act
        List<Client> foundClients = clientRepository.searchClients("ivanov");

        // Assert
        assertThat(foundClients).hasSize(1);
        assertThat(foundClients.get(0).getEmail()).isEqualTo("ivanov@mail.com");
    }

    @Test
    void shouldSearchClientsCaseInsensitive() {
        // Act
        List<Client> foundClients = clientRepository.searchClients("ИВАНОВ");

        // Assert
        assertThat(foundClients).hasSize(1);
        assertThat(foundClients.get(0).getFullName()).isEqualTo("Иванов Иван Иванович");
    }

    @Test
    void shouldReturnEmptyListForNonMatchingSearch() {
        // Act
        List<Client> foundClients = clientRepository.searchClients("nonexistent");

        // Assert
        assertThat(foundClients).isEmpty();
    }

    @Test
    void shouldCheckExistsByPhoneNumber() {
        // Act & Assert
        assertThat(clientRepository.existsByPhoneNumber("+79123456789")).isTrue();
        assertThat(clientRepository.existsByPhoneNumber("+79999999999")).isFalse();
    }

    @Test
    void shouldCheckExistsByEmail() {
        // Act & Assert
        assertThat(clientRepository.existsByEmail("ivanov@mail.com")).isTrue();
        assertThat(clientRepository.existsByEmail("nonexistent@mail.com")).isFalse();
    }

    @Test
    void shouldHandleEmptySearchString() {
        // Act
        List<Client> foundClients = clientRepository.searchClients("");

        // Assert - должен вернуть всех клиентов или пустой список (зависит от логики)
        assertThat(foundClients).isNotNull();
    }

    @Test
    void shouldFindByCreatedAtExact() {
        // Arrange
        LocalDateTime exactTime = client2.getCreatedAt();

        // Ищем с небольшой погрешностью
        List<Client> foundClients = clientRepository.findByCreatedAtBetween(
                exactTime.minusSeconds(1),
                exactTime.plusSeconds(1)
        );

        // Assert
        assertThat(foundClients).hasSize(1);
        assertThat(foundClients.get(0)).isEqualTo(client2);
    }

    @Test
    void shouldFindByUpdatedAtExact() {
        // Arrange
        LocalDateTime exactTime = client1.getUpdatedAt();

        // Ищем с небольшой погрешностью
        List<Client> foundClients = clientRepository.findByUpdatedAtBetween(
                exactTime.minusSeconds(1),
                exactTime.plusSeconds(1)
        );

        // Assert
        assertThat(foundClients).hasSize(1);
        assertThat(foundClients.get(0)).isEqualTo(client1);
    }

    @Test
    void shouldReturnEmptyForNonExistentIdWithPassport() {
        // Act
        Optional<Client> foundClient = clientRepository.findByIdWithPassport(999L);

        // Assert
        assertThat(foundClient).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNonExistentIdWithReservations() {
        // Act
        Optional<Client> foundClient = clientRepository.findByIdWithTicketReservations(999L);

        // Assert
        assertThat(foundClient).isEmpty();
    }

    @Test
    void shouldHandleClientWithNullEmail() {
        Passport passportForClientWithoutEmail = new Passport("1234", "123456");
        // Arrange
        Client clientWithoutEmail = new Client("Тестов Клиент", "+79423456789", passportForClientWithoutEmail);
        entityManager.persist(clientWithoutEmail);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Client> foundByPhone = clientRepository.findByPhoneNumber("+79423456789");
        List<Client> foundBySearch = clientRepository.searchClients("79423456789");

        // Assert
        assertThat(foundByPhone).isPresent();
        assertThat(foundBySearch).hasSize(1);
    }
    @Test
    void shouldFindByCreatedAtWithTolerance() {
        // Перезагружаем клиента чтобы получить точные даты из БД
        Client reloadedClient = entityManager.find(Client.class, client1.getId());
        LocalDateTime exactCreatedAt = reloadedClient.getCreatedAt();

        // Ищем с небольшой погрешностью (1 секунда)
        LocalDateTime start = exactCreatedAt.minusSeconds(1);
        LocalDateTime end = exactCreatedAt.plusSeconds(1);

        List<Client> foundClients = clientRepository.findByCreatedAtBetween(start, end);

        assertThat(foundClients).hasSize(1);
        assertThat(foundClients.get(0)).isEqualTo(client1);
    }
}