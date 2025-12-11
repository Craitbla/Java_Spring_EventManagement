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
class EventRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    private Event event1;

    @Nested
    class EventRepositoryBasicTests {

        @BeforeEach
        void setUp() {
            event1 = new Event("Концерт рок-группы",
                    LocalDate.now().plusDays(10),
                    100,
                    BigDecimal.valueOf(1500),
                    EventStatus.PLANNED,
                    "Концерт известной рок-группы");

            entityManager.persist(event1);
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        void shouldFindByNameIgnoreCase() {
            List<Event> foundEvents = eventRepository.findByNameIgnoreCase("концерт рок-группы");
            assertThat(foundEvents).hasSize(1);
            assertThat(foundEvents.get(0).getName()).isEqualTo("Концерт рок-группы");
        }

        @Test
        void shouldFindByNameContainingIgnoreCase() {
            List<Event> foundEvents = eventRepository.findByNameContainingIgnoreCase("рок-группы");
            assertThat(foundEvents).hasSize(1);
            assertThat(foundEvents.get(0).getName()).contains("рок-группы");
        }

        @Test
        void shouldFindByDate() {
            LocalDate targetDate = LocalDate.now().plusDays(10);
            List<Event> foundEvents = eventRepository.findByDate(targetDate);
            assertThat(foundEvents).hasSize(1);
            assertThat(foundEvents.get(0).getDate()).isEqualTo(targetDate);
        }

        @Test
        void shouldFindByNameAndDate() {
            Optional<Event> foundEvent = eventRepository.findByNameAndDate("Концерт рок-группы", LocalDate.now().plusDays(10));
            assertThat(foundEvent).isPresent();
            assertThat(foundEvent.get().getName()).isEqualTo("Концерт рок-группы");
        }

        @Test
        void shouldFindByTicketPrice() {
            List<Event> foundEvents = eventRepository.findByTicketPrice(BigDecimal.valueOf(1500));
            assertThat(foundEvents).hasSize(1);
            assertThat(foundEvents.get(0).getTicketPrice()).isBetween(BigDecimal.valueOf(1500-1), BigDecimal.valueOf(1500+1));
        }

        @Test
        void shouldFindByTicketPriceLessThan() {
            List<Event> foundEvents = eventRepository.findByTicketPriceLessThan(BigDecimal.valueOf(2000));
            assertThat(foundEvents).hasSize(1);
            assertThat(foundEvents.get(0).getTicketPrice()).isLessThan(BigDecimal.valueOf(2000));
        }

        @Test
        void shouldFindByTicketPriceBetween() {
            List<Event> foundEvents = eventRepository.findByTicketPriceBetween(BigDecimal.valueOf(1000), BigDecimal.valueOf(2000));
            assertThat(foundEvents).hasSize(1);
            assertThat(foundEvents.get(0).getTicketPrice()).isBetween(BigDecimal.valueOf(1000), BigDecimal.valueOf(2000));
        }

        @Test
        void shouldFindByStatus() {
            List<Event> foundEvents = eventRepository.findByStatus(EventStatus.PLANNED);
            assertThat(foundEvents).hasSize(1);
            assertThat(foundEvents.get(0).getStatus()).isEqualTo(EventStatus.PLANNED);
        }

        @Test
        void shouldFindByStatusIn() {
            List<Event> foundEvents = eventRepository.findByStatusIn(List.of(EventStatus.PLANNED, EventStatus.ONGOING));
            assertThat(foundEvents).hasSize(1);
            assertThat(foundEvents.get(0).getStatus()).isEqualTo(EventStatus.PLANNED);
        }

        @Test
        void shouldFindByDescription() {
            List<Event> foundEvents = eventRepository.findByDescription("Концерт известной рок-группы");
            assertThat(foundEvents).hasSize(1);
            assertThat(foundEvents.get(0).getDescription()).isEqualTo("Концерт известной рок-группы");
        }

        @Test
        void shouldCheckExistsByNameAndDate() {
            boolean exists = eventRepository.existsByNameAndDate("Концерт рок-группы", LocalDate.now().plusDays(10));
            assertThat(exists).isTrue();
        }

        @Test
        void shouldNotExistForNonMatchingNameAndDate() {
            boolean exists = eventRepository.existsByNameAndDate("Несуществующий концерт", LocalDate.now().plusDays(10));
            assertThat(exists).isFalse();
        }
    }

    @Nested
    class EventRepositoryDateTimeTests {
        private Event event2, event3;

        @BeforeEach
        void setUp() {
            LocalDateTime now = LocalDateTime.now();

            event1 = Event.createForTesting("Концерт 1",
                    LocalDate.now().plusDays(5),
                    1,
                    BigDecimal.valueOf(1000),
                    EventStatus.PLANNED,
                    "Описание 1",
                    now.minusDays(2),
                    now.minusDays(1));

            event2 = Event.createForTesting("Концерт 2",
                    LocalDate.now().plusDays(3),
                    1000,
                    BigDecimal.valueOf(2000),
                    EventStatus.ONGOING,
                    "Описание 2",
                    now.minusDays(1),
                    now);

            event3 = Event.createForTesting("Концерт 3",
                    LocalDate.now().plusDays(1),
                    100,
                    BigDecimal.valueOf(3000),
                    EventStatus.COMPLETED,
                    "Описание 3",
                    now,
                    now);

            entityManager.persist(event1);
            entityManager.persist(event2);
            entityManager.persist(event3);
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        void shouldFindByCreatedAtBefore() {
            LocalDateTime threshold = LocalDateTime.now().minusHours(12);
            List<Event> foundEvents = eventRepository.findByCreatedAtBefore(threshold);
            assertThat(foundEvents).hasSize(2);
        }

        @Test
        void shouldFindByCreatedAtBetween() {
            LocalDateTime start = LocalDateTime.now().minusDays(3);
            LocalDateTime end = LocalDateTime.now().minusDays(1).plusHours(1);
            List<Event> foundEvents = eventRepository.findByCreatedAtBetween(start, end);
            assertThat(foundEvents).hasSize(2);
        }

        @Test
        void shouldFindByUpdatedAtAfter() {
            LocalDateTime threshold = LocalDateTime.now().minusHours(12);
            List<Event> foundEvents = eventRepository.findByUpdatedAtAfter(threshold);
            assertThat(foundEvents).hasSize(2);
        }

        @Test
        void shouldFindByCreatedAtExact() {
            LocalDateTime exactTime = event2.getCreatedAt();
            List<Event> foundEvents = eventRepository.findByCreatedAtBetween(
                    exactTime.minusSeconds(1), exactTime.plusSeconds(1));
            assertThat(foundEvents).hasSize(1);
            assertThat(foundEvents.get(0)).isEqualTo(event2);
        }

        @Test
        void shouldFindByUpdatedAtExact() {
            LocalDateTime exactTime = event1.getUpdatedAt();
            List<Event> foundEvents = eventRepository.findByUpdatedAtBetween(
                    exactTime.minusSeconds(1), exactTime.plusSeconds(1));
            assertThat(foundEvents).hasSize(1);
            assertThat(foundEvents.get(0)).isEqualTo(event1);
        }
    }

    @Nested
    class EventRepositoryAdvancedTests {
        private Passport passport1;
        private Client client1;

        @BeforeEach
        void setUp() {
            passport1 = new Passport("1234", "123456");
            client1 = new Client("Иванов Иван Иванович", "+79123456789", "ivanov@mail.com", passport1);
            event1 = new Event("Тестовое мероприятие",
                    LocalDate.now().plusDays(7),
                    100,
                    BigDecimal.valueOf(500),
                    EventStatus.PLANNED,
                    "Тестовое описание");


            entityManager.persist(passport1);
            entityManager.persist(client1);
            entityManager.persist(event1);
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        void shouldFindByIdWithTicketReservations() {
            TicketReservation reservation = new TicketReservation(3, BookingStatus.CONFIRMED, client1, event1);

            entityManager.persist(reservation);
            entityManager.flush();
            entityManager.clear();


            Optional<Event> foundEvent = eventRepository.findByIdWithTicketReservations(event1.getId());

            assertThat(foundEvent).isPresent();
            assertThat(foundEvent.get().getTicketReservations()).hasSize(1);
            assertThat(foundEvent.get().getTicketReservations().get(0).getNumberOfTickets()).isEqualTo(3);
        }

        @Test
        void shouldCountConfirmedTicketsByEventId() {
            TicketReservation reservation1 = new TicketReservation(2, BookingStatus.CONFIRMED, client1, event1);
            TicketReservation reservation2 = new TicketReservation(3, BookingStatus.CONFIRMED, client1, event1);
            TicketReservation reservation3 = new TicketReservation(1, BookingStatus.CANCELED, client1, event1);

            entityManager.persist(reservation1);
            entityManager.persist(reservation2);
            entityManager.persist(reservation3);

            entityManager.flush();
            entityManager.clear();

            Integer confirmedCount = eventRepository.countConfirmedTicketsByEventId(event1.getId()).intValue();

            assertThat(confirmedCount).isEqualTo(5);
        }

        @Test
        void shouldReturnZeroConfirmedTicketsForNonExistentEvent() {
            Integer confirmedCount = eventRepository.countConfirmedTicketsByEventId(999L).intValue();
            assertThat(confirmedCount).isEqualTo(0);
        }

        @Test
        void shouldReturnEmptyForNonExistentIdWithReservations() {
            Optional<Event> foundEvent = eventRepository.findByIdWithTicketReservations(999L);
            assertThat(foundEvent).isEmpty();
        }
        @Test
        void demonstrateJoinVsLeftJoinDifference() {
            Event eventWithoutReservations = new Event("Концерт без броней",
                    LocalDate.now().plusDays(5),
                    100,
                    BigDecimal.valueOf(500),
                    EventStatus.PLANNED,
                    "У этого события нет броней");

            entityManager.persist(eventWithoutReservations);
            entityManager.flush();
            entityManager.clear();

            Optional<Event> foundEvent = eventRepository.findByIdWithTicketReservations(eventWithoutReservations.getId());

            assertThat(foundEvent).isPresent();
            assertThat(foundEvent.get().getTicketReservations()).isEmpty();
        }
    }
}