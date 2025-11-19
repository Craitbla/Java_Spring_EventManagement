package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
class ClientRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    private Client client1;

    @Nested
    class ClientRepositoryBasicTests {

        private Passport passport1;

        @BeforeEach
        void setUp() {
            passport1 = new Passport("1234", "567890");
            entityManager.persist(passport1);

            client1 = new Client("Иванов Иван Иванович", "+79123456789", "ivanov@mail.com", passport1);
            entityManager.persist(client1);
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        void shouldFindByFullNameIgnoreCase() {
            List<Client> foundClients = clientRepository.findByFullNameIgnoreCase("иванов иван иванович");
            assertThat(foundClients).hasSize(1);
            assertThat(foundClients.get(0).getFullName()).isEqualTo("Иванов Иван Иванович");
        }

        @Test
        void shouldFindByFullNameContainingIgnoreCase() {
            List<Client> foundClients = clientRepository.findByFullNameContainingIgnoreCase("иван");
            assertThat(foundClients).hasSize(1);
            assertThat(foundClients.get(0).getFullName()).contains("Иван");
        }

        @Test
        void shouldFindByPhoneNumber() {
            Optional<Client> foundClient = clientRepository.findByPhoneNumber("+79123456789");
            assertThat(foundClient).isPresent();
            assertThat(foundClient.get().getPhoneNumber()).isEqualTo("+79123456789");
        }

        @Test
        void shouldNotFindByNonExistentPhoneNumber() {
            Optional<Client> foundClient = clientRepository.findByPhoneNumber("+79999999999");
            assertThat(foundClient).isEmpty();
        }

        @Test
        void shouldFindByEmail() {
            Optional<Client> foundClient = clientRepository.findByEmail("ivanov@mail.com");
            assertThat(foundClient).isPresent();
            assertThat(foundClient.get().getEmail()).isEqualTo("ivanov@mail.com");
        }

        @Test
        void shouldFindByPassport() { ///////////////////////////////
            Optional<Client> foundClient = clientRepository.findByPassport(passport1);
            assertThat(foundClient.isPresent()).isTrue();
            assertThat(foundClient.get().getPassport()).isEqualTo(passport1);
        }

        @Test
        void shouldCheckExistsByPhoneNumber() {
            assertThat(clientRepository.existsByPhoneNumber("+79123456789")).isTrue();
            assertThat(clientRepository.existsByPhoneNumber("+79999999999")).isFalse();
        }

        @Test
        void shouldCheckExistsByEmail() {
            assertThat(clientRepository.existsByEmail("ivanov@mail.com")).isTrue();
            assertThat(clientRepository.existsByEmail("nonexistent@mail.com")).isFalse();
        }

        @Test
        void shouldHandleClientWithNullEmail() {
            Passport newPassport = new Passport("9999", "999999");
            Client clientWithoutEmail = new Client("Тестов Клиент", "+79423456789", newPassport);

            entityManager.persist(newPassport);
            entityManager.persist(clientWithoutEmail);
            entityManager.flush();
            entityManager.clear();

            Optional<Client> foundByPhone = clientRepository.findByPhoneNumber("+79423456789");
            List<Client> foundBySearch = clientRepository.searchClients("79423456789");

            assertThat(foundByPhone).isPresent();
            assertThat(foundBySearch).hasSize(1);
        }
    }

    @Nested
    class ClientRepositoryDateTimeTests {
        private Client client2, client3;

        @BeforeEach
        void setUp() {
            LocalDateTime now = LocalDateTime.now();

            Passport passport1 = new Passport("1234", "567890");
            Passport passport2 = new Passport("5678", "901234");
            Passport passport3 = new Passport("7237", "215376");

            entityManager.persist(passport1);
            entityManager.persist(passport2);
            entityManager.persist(passport3);

            // Используем фабричный метод для установки конкретных временных меток
            client1 = Client.createForTesting("Иванов Иван", "+79123456789", "ivanov@mail.com",
                    passport1, now.minusDays(2), now.minusDays(1));
            client2 = Client.createForTesting("Петров Петр", "+79223456789", "petrov@mail.com",
                    passport2, now.minusDays(1), now);
            client3 = Client.createForTesting("Сидорова Анна", "+79323456789", "sidorova@mail.com",
                    passport3, now, now);

            entityManager.persist(client1);
            entityManager.persist(client2);
            entityManager.persist(client3);
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        void shouldFindByCreatedAtBefore() {
            LocalDateTime threshold = LocalDateTime.now().minusHours(12);
            List<Client> foundClients = clientRepository.findByCreatedAtBefore(threshold);
            assertThat(foundClients).hasSize(2); // client1 и client2
        }

        @Test
        void shouldFindByCreatedAtBetween() {
            LocalDateTime start = LocalDateTime.now().minusDays(3);
            LocalDateTime end = LocalDateTime.now().minusDays(1).plusHours(1);
            List<Client> foundClients = clientRepository.findByCreatedAtBetween(start, end);
            assertThat(foundClients).hasSize(2); // client1 и client2
        }

        @Test
        void shouldFindByUpdatedAtAfter() {
            LocalDateTime threshold = LocalDateTime.now().minusHours(12);
            List<Client> foundClients = clientRepository.findByUpdatedAtAfter(threshold);
            assertThat(foundClients).hasSize(2); // client2 и client3
        }

        @Test
        void shouldFindByCreatedAtExact() {
            LocalDateTime exactTime = client2.getCreatedAt();
            List<Client> foundClients = clientRepository.findByCreatedAtBetween(
                    exactTime.minusSeconds(1), exactTime.plusSeconds(1));
            assertThat(foundClients).hasSize(1);
            assertThat(foundClients.get(0)).isEqualTo(client2);
        }

        @Test
        void shouldFindByUpdatedAtExact() {
            LocalDateTime exactTime = client1.getUpdatedAt();
            List<Client> foundClients = clientRepository.findByUpdatedAtBetween(
                    exactTime.minusSeconds(1), exactTime.plusSeconds(1));
            assertThat(foundClients).hasSize(1);
            assertThat(foundClients.get(0)).isEqualTo(client1);
        }

        @Test
        void shouldFindByCreatedAtWithTolerance() {
            Client reloadedClient = entityManager.find(Client.class, client1.getId());
            LocalDateTime exactCreatedAt = reloadedClient.getCreatedAt();

            List<Client> foundClients = clientRepository.findByCreatedAtBetween(
                    exactCreatedAt.minusSeconds(1), exactCreatedAt.plusSeconds(1));

            assertThat(foundClients).hasSize(1);
            assertThat(foundClients.get(0)).isEqualTo(client1);
        }
    }

    @Nested
    class ClientRepositoryAdvancedTests {

        @BeforeEach
        void setUp() {
            Passport passport1 = new Passport("1234", "567890");
            entityManager.persist(passport1);

            client1 = new Client("Иванов Иван Иванович", "+79123456789", "ivanov@mail.com", passport1);
            entityManager.persist(client1);
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        void shouldFindByIdWithPassport() {
            Optional<Client> foundClient = clientRepository.findByIdWithPassport(client1.getId());

            assertThat(foundClient).isPresent();
            assertThat(foundClient.get().getPassport()).isNotNull();
            assertThat(foundClient.get().getPassport().getSeries()).isEqualTo("1234");
            assertThat(foundClient.get().getPassport().getClient()).isNotNull();
        }

        @Test
        void shouldFindByIdWithTicketReservations() {
            // Создаем резервацию для клиента
            TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
            client1.addTicketReservation(reservation);

            Event event = new Event("Концерт", LocalDate.now().plusDays(10),
                    BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
            entityManager.persist(event);
            event.addTicketReservation(reservation);

            entityManager.persist(reservation);
            entityManager.flush();
            entityManager.clear();

            Optional<Client> foundClient = clientRepository.findByIdWithTicketReservations(client1.getId());

            assertThat(foundClient).isPresent();
            assertThat(foundClient.get().getTicketReservations()).hasSize(1);
            assertThat(foundClient.get().getTicketReservations().get(0).getNumberOfTickets()).isEqualTo(2);
        }

        @Test
        void shouldSearchClientsByName() {
            List<Client> foundClients = clientRepository.searchClients("Иванов");
            assertThat(foundClients).hasSize(1);
            assertThat(foundClients.get(0).getFullName()).isEqualTo("Иванов Иван Иванович");
        }

        @Test
        void shouldSearchClientsByPhoneNumber() {
            List<Client> foundClients = clientRepository.searchClients("912345");
            assertThat(foundClients).hasSize(1);
            assertThat(foundClients.get(0).getPhoneNumber()).isEqualTo("+79123456789");
        }

        @Test
        void shouldSearchClientsByEmail() {
            List<Client> foundClients = clientRepository.searchClients("ivanov");
            assertThat(foundClients).hasSize(1);
            assertThat(foundClients.get(0).getEmail()).isEqualTo("ivanov@mail.com");
        }

        @Test
        void shouldSearchClientsCaseInsensitive() {
            List<Client> foundClients = clientRepository.searchClients("ИВАНОВ");
            assertThat(foundClients).hasSize(1);
            assertThat(foundClients.get(0).getFullName()).isEqualTo("Иванов Иван Иванович");
        }

        @Test
        void shouldReturnEmptyListForNonMatchingSearch() {
            List<Client> foundClients = clientRepository.searchClients("nonexistent");
            assertThat(foundClients).isEmpty();
        }

        @Test
        void shouldHandleEmptySearchString() {
            List<Client> foundClients = clientRepository.searchClients("");
            assertThat(foundClients).isNotNull();
        }

        @Test
        void shouldReturnEmptyForNonExistentIdWithPassport() {
            Optional<Client> foundClient = clientRepository.findByIdWithPassport(999L);
            assertThat(foundClient).isEmpty();
        }

        @Test
        void shouldReturnEmptyForNonExistentIdWithReservations() {
            Optional<Client> foundClient = clientRepository.findByIdWithTicketReservations(999L);
            assertThat(foundClient).isEmpty();
        }
    }
}