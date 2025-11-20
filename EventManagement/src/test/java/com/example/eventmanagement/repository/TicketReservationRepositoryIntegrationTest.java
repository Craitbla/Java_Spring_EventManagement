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
class TicketReservationRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketReservationRepository ticketReservationRepository;

    private TicketReservation reservation1;
    private Client client1;
    private Event event1;

    @Nested
    class TicketReservationRepositoryBasicTests {

        @BeforeEach
        void setUp() {
            Passport passport = new Passport("1234", "567890");
            entityManager.persist(passport);

            client1 = new Client("Иван Иванов", "+79123456789", "ivan@mail.com", passport);
            entityManager.persist(client1);

            event1 = new Event("Концерт",
                    LocalDate.now().plusDays(10),100,
                    BigDecimal.valueOf(1000),
                    EventStatus.PLANNED,
                    "Концерт классической музыки");
            entityManager.persist(event1);

            reservation1 = new TicketReservation(2, BookingStatus.CONFIRMED, client1, event1);

            entityManager.persist(reservation1);
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        void shouldFindByNumberOfTickets() {
            List<TicketReservation> foundReservations = ticketReservationRepository.findByNumberOfTickets(2);
            assertThat(foundReservations).hasSize(1);
            assertThat(foundReservations.get(0).getNumberOfTickets()).isEqualTo(2);
        }

        @Test
        void shouldFindByClientId() {
            List<TicketReservation> foundReservations = ticketReservationRepository.findByClientId(client1.getId());
            assertThat(foundReservations).hasSize(1);
            assertThat(foundReservations.get(0).getClient().getId()).isEqualTo(client1.getId());
        }

        @Test
        void shouldFindByEventId() {
            List<TicketReservation> foundReservations = ticketReservationRepository.findByEventId(event1.getId());
            assertThat(foundReservations).hasSize(1);
            assertThat(foundReservations.get(0).getEvent().getId()).isEqualTo(event1.getId());
        }

        @Test
        void shouldFindByNumberOfTicketsLessThan() {
            List<TicketReservation> foundReservations = ticketReservationRepository.findByNumberOfTicketsLessThan(3);
            assertThat(foundReservations).hasSize(1);
            assertThat(foundReservations.get(0).getNumberOfTickets()).isLessThan(3);
        }

        @Test
        void shouldFindByNumberOfTicketsBetween() {
            List<TicketReservation> foundReservations = ticketReservationRepository.findByNumberOfTicketsBetween(1, 3);
            assertThat(foundReservations).hasSize(1);
            assertThat(foundReservations.get(0).getNumberOfTickets()).isBetween(1, 3);
        }

        @Test
        void shouldFindByNumberOfTicketsGreaterThan() {
            List<TicketReservation> foundReservations = ticketReservationRepository.findByNumberOfTicketsGreaterThan(1);
            assertThat(foundReservations).hasSize(1);
            assertThat(foundReservations.get(0).getNumberOfTickets()).isGreaterThan(1);
        }

        @Test
        void shouldFindByBookingStatus() {
            List<TicketReservation> foundReservations = ticketReservationRepository.findByBookingStatus(BookingStatus.CONFIRMED);
            assertThat(foundReservations).hasSize(1);
            assertThat(foundReservations.get(0).getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        void shouldFindByBookingStatusIn() {
            List<TicketReservation> foundReservations = ticketReservationRepository.findByBookingStatusIn(
                    List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING_CONFIRMATION));
            assertThat(foundReservations).hasSize(1);
            assertThat(foundReservations.get(0).getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        void shouldFindByClientIdAndEventIdAndBookingStatus() {
            List<TicketReservation> foundReservations = ticketReservationRepository.findByClientIdAndEventIdAndBookingStatus(
                    client1.getId(), event1.getId(), BookingStatus.CONFIRMED);
            assertThat(foundReservations).hasSize(1);
            assertThat(foundReservations.get(0).getClient().getId()).isEqualTo(client1.getId());
            assertThat(foundReservations.get(0).getEvent().getId()).isEqualTo(event1.getId());
            assertThat(foundReservations.get(0).getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }
    }

    @Nested
    class TicketReservationRepositoryDateTimeTests {
        private TicketReservation reservation2, reservation3;

        @BeforeEach
        void setUp() {
            LocalDateTime now = LocalDateTime.now();

            Passport passport = new Passport("1234", "567890");
            entityManager.persist(passport);

            client1 = new Client("Иван Иванов", "+79123456789", "ivan@mail.com", passport);
            entityManager.persist(client1);

            event1 = new Event("Концерт",
                    LocalDate.now().plusDays(10),100,
                    BigDecimal.valueOf(1000),
                    EventStatus.PLANNED,
                    "Концерт классической музыки");
            entityManager.persist(event1);

            reservation1 = TicketReservation.createForTestingAll(1, BookingStatus.PENDING_CONFIRMATION, client1,event1,
                    now.minusDays(2), now.minusDays(1));
            reservation2 = TicketReservation.createForTestingAll(2, BookingStatus.CONFIRMED,client1,event1,
                    now.minusDays(1), now);
            reservation3 = TicketReservation.createForTestingAll(3, BookingStatus.CANCELED,client1,event1,
                    now, now);



            entityManager.persist(reservation1);
            entityManager.persist(reservation2);
            entityManager.persist(reservation3);
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        void shouldFindByCreatedAtBefore() {
            LocalDateTime threshold = LocalDateTime.now().minusHours(12);
            List<TicketReservation> foundReservations = ticketReservationRepository.findByCreatedAtBefore(threshold);
            assertThat(foundReservations).hasSize(2);
        }

        @Test
        void shouldFindByCreatedAtBetween() {
            LocalDateTime start = LocalDateTime.now().minusDays(3);
            LocalDateTime end = LocalDateTime.now().minusDays(1).plusHours(1);
            List<TicketReservation> foundReservations = ticketReservationRepository.findByCreatedAtBetween(start, end);
            assertThat(foundReservations).hasSize(2);
        }

        @Test
        void shouldFindByCreatedAtAfter() {
            LocalDateTime threshold = LocalDateTime.now().minusHours(12);
            List<TicketReservation> foundReservations = ticketReservationRepository.findByCreatedAtAfter(threshold);
            assertThat(foundReservations).hasSize(1);
        }

        @Test
        void shouldFindByUpdatedAtBefore() {
            LocalDateTime threshold = LocalDateTime.now().minusHours(12);
            List<TicketReservation> foundReservations = ticketReservationRepository.findByUpdatedAtBefore(threshold);
            assertThat(foundReservations).hasSize(1);
        }

        @Test
        void shouldFindByUpdatedAtBetween() {
            LocalDateTime start = LocalDateTime.now().minusDays(3);
            LocalDateTime end = LocalDateTime.now().minusDays(1).plusHours(1);
            List<TicketReservation> foundReservations = ticketReservationRepository.findByUpdatedAtBetween(start, end);
            assertThat(foundReservations).hasSize(1);
        }

        @Test
        void shouldFindByUpdatedAtAfter() {
            LocalDateTime threshold = LocalDateTime.now().minusHours(12);
            List<TicketReservation> foundReservations = ticketReservationRepository.findByUpdatedAtAfter(threshold);
            assertThat(foundReservations).hasSize(2);
        }

        @Test
        void shouldFindByCreatedAtExact() {
            LocalDateTime exactTime = reservation2.getCreatedAt();
            List<TicketReservation> foundReservations = ticketReservationRepository.findByCreatedAtBetween(
                    exactTime.minusSeconds(1), exactTime.plusSeconds(1));
            assertThat(foundReservations).hasSize(1);
            assertThat(foundReservations.get(0)).isEqualTo(reservation2);
        }

        @Test
        void shouldFindByUpdatedAtExact() {
            LocalDateTime exactTime = reservation1.getUpdatedAt();
            List<TicketReservation> foundReservations = ticketReservationRepository.findByUpdatedAtBetween(
                    exactTime.minusSeconds(1), exactTime.plusSeconds(1));
            assertThat(foundReservations).hasSize(1);
            assertThat(foundReservations.get(0)).isEqualTo(reservation1);
        }
    }

    @Nested
    class TicketReservationRepositoryAdvancedTests {

        @BeforeEach
        void setUp() {
            Passport passport = new Passport("1234", "567890");
            entityManager.persist(passport);

            client1 = new Client("Иван Иванов", "+79123456789", "ivan@mail.com", passport);
            entityManager.persist(client1);

            event1 = new Event("Концерт",
                    LocalDate.now().plusDays(10),100,
                    BigDecimal.valueOf(1000),
                    EventStatus.PLANNED,
                    "Концерт классической музыки");
            entityManager.persist(event1);

            reservation1 = new TicketReservation(2, BookingStatus.CONFIRMED,client1,event1);

            entityManager.persist(reservation1);
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        void shouldFindByIdWithClient() {
            Optional<TicketReservation> foundReservation = ticketReservationRepository.findByIdWithClient(reservation1.getId());

            assertThat(foundReservation).isPresent();
            assertThat(foundReservation.get().getClient()).isNotNull();
            assertThat(foundReservation.get().getClient().getFullName()).isEqualTo("Иван Иванов");
        }

        @Test
        void shouldFindByIdWithEvent() {
            Optional<TicketReservation> foundReservation = ticketReservationRepository.findByIdWithEvent(reservation1.getId());

            assertThat(foundReservation).isPresent();
            assertThat(foundReservation.get().getEvent()).isNotNull();
            assertThat(foundReservation.get().getEvent().getName()).isEqualTo("Концерт");
        }

        @Test
        void shouldFindByIdWithClientAndEvent() {
            Optional<TicketReservation> foundReservation = ticketReservationRepository.findByIdWithClientAndEvent(reservation1.getId());

            assertThat(foundReservation).isPresent();
            assertThat(foundReservation.get().getClient()).isNotNull();
            assertThat(foundReservation.get().getEvent()).isNotNull();
            assertThat(foundReservation.get().getClient().getFullName()).isEqualTo("Иван Иванов");
            assertThat(foundReservation.get().getEvent().getName()).isEqualTo("Концерт");
        }

        @Test
        void shouldReturnEmptyForNonExistentIdWithClient() {
            Optional<TicketReservation> foundReservation = ticketReservationRepository.findByIdWithClient(999L);
            assertThat(foundReservation).isEmpty();
        }

        @Test
        void shouldReturnEmptyForNonExistentIdWithEvent() {
            Optional<TicketReservation> foundReservation = ticketReservationRepository.findByIdWithEvent(999L);
            assertThat(foundReservation).isEmpty();
        }

        @Test
        void shouldReturnEmptyForNonExistentIdWithClientAndEvent() {
            Optional<TicketReservation> foundReservation = ticketReservationRepository.findByIdWithClientAndEvent(999L);
            assertThat(foundReservation).isEmpty();
        }
    }
}