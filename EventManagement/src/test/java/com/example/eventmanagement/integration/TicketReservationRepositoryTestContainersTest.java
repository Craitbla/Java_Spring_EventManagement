package com.example.eventmanagement.integration;

import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.repository.TicketReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("testcontainers")
class TicketReservationRepositoryTestContainersTest extends BaseTestcontainersTest {

    @Autowired
    private TicketReservationRepository ticketReservationRepository;
    @Autowired
    private TestEntityManager entityManager;

    private Client client;
    private Event event;

    @BeforeEach
    void setUp() {
        if (ticketReservationRepository != null) {
            ticketReservationRepository.deleteAll();
        }

        Passport passport = new Passport("1234", "567890");
        client = new Client("Test Client", "+79123456789", "test@mail.com", passport);
        event = new Event("Test Event",
                LocalDate.now().plusDays(5), 100,
                BigDecimal.valueOf(1000),
                EventStatus.PLANNED,
                "Test Description");

        // Сохраняем через entityManager в тестовом методе
    }

    @Test
    void shouldSaveAndRetrieveReservationWithRealPostgreSQL() {
        entityManager.persist(client.getPassport());
        entityManager.persist(client);
        entityManager.persist(event);

        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED, client,event);

        TicketReservation saved = ticketReservationRepository.save(reservation);

        assertThat(saved.getId()).isNotNull();

        Optional<TicketReservation> found = ticketReservationRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getNumberOfTickets()).isEqualTo(2);
        assertThat(found.get().getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(found.get().getClient().getFullName()).isEqualTo("Test Client");
        assertThat(found.get().getEvent().getName()).isEqualTo("Test Event");
    }

    @Test
    void shouldFindByClientIdAndEventId() {
        entityManager.persist(client.getPassport());
        entityManager.persist(client);
        entityManager.persist(event);

        TicketReservation reservation = new TicketReservation(3, BookingStatus.PENDING_CONFIRMATION, client, event);
        ticketReservationRepository.save(reservation);

        List<TicketReservation> foundReservations = ticketReservationRepository.findByClientIdAndEventIdAndBookingStatus(
                client.getId(), event.getId(), BookingStatus.PENDING_CONFIRMATION);

        assertThat(foundReservations).hasSize(1);
        assertThat(foundReservations.get(0).getNumberOfTickets()).isEqualTo(3);
    }
}