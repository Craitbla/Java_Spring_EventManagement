package com.example.eventmanagement.entity;

import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class EventEntityIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveEventWithAllFields() {
        Event event = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Music concert");

        entityManager.persistAndFlush(event);

        assertThat(event.getId()).isNotNull();
        assertThat(event.getName()).isEqualTo("Concert");
        assertThat(event.getDate()).isAfter(LocalDate.now());
        assertThat(event.getTicketPrice()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(event.getStatus()).isEqualTo(EventStatus.PLANNED);
        assertThat(event.getDescription()).isEqualTo("Music concert");
        assertThat(event.getCreatedAt()).isNotNull();
        assertThat(event.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldSetDefaultValuesOnPersist() {
        Event event = new Event();
        event.setName("Test Event");
        event.setDate(LocalDate.now().plusDays(5));

        entityManager.persistAndFlush(event);

        assertThat(event.getTicketPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(event.getStatus()).isEqualTo(EventStatus.PLANNED);
        assertThat(event.getCreatedAt()).isNotNull();
        assertThat(event.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldEnforceUniqueNameAndDateConstraint() {
        LocalDate sameDate = LocalDate.now().plusDays(10);
        Event event1 = new Event("Concert", sameDate, BigDecimal.valueOf(100), EventStatus.PLANNED, "Desc1");
        Event event2 = new Event("Concert", sameDate, BigDecimal.valueOf(150), EventStatus.CANCELED, "Desc2");

        entityManager.persistAndFlush(event1);

        assertThatThrownBy(() -> entityManager.persistAndFlush(event2))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldAllowSameNameWithDifferentDates() {
        Event event1 = new Event("Concert", LocalDate.now().plusDays(10), BigDecimal.valueOf(100), EventStatus.PLANNED, "Desc1");
        Event event2 = new Event("Concert", LocalDate.now().plusDays(11), BigDecimal.valueOf(150), EventStatus.PLANNED, "Desc2");

        entityManager.persistAndFlush(event1);
        entityManager.persistAndFlush(event2);

        assertThat(event1.getId()).isNotNull();
        assertThat(event2.getId()).isNotNull();
        assertThat(event1.getName()).isEqualTo(event2.getName());
        assertThat(event1.getDate()).isNotEqualTo(event2.getDate());
    }

    @Test
    void shouldUpdateEventFields() {
        Event event = new Event("Old Name", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Old Desc");
        entityManager.persistAndFlush(event);

        event.setName("New Name");
        event.setTicketPrice(BigDecimal.valueOf(200));
        event.setStatus(EventStatus.CANCELED);
        event.setDescription("New Description");

        Event updatedEvent = entityManager.persistAndFlush(event);

        assertThat(updatedEvent.getName()).isEqualTo("New Name");
        assertThat(updatedEvent.getTicketPrice()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(updatedEvent.getStatus()).isEqualTo(EventStatus.CANCELED);
        assertThat(updatedEvent.getDescription()).isEqualTo("New Description");
        assertThat(updatedEvent.getUpdatedAt()).isAfter(updatedEvent.getCreatedAt());
    }

    @Test
    void shouldSaveEventWithNullDescription() {
        Event event = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, null);

        entityManager.persistAndFlush(event);

        assertThat(event.getId()).isNotNull();
        assertThat(event.getDescription()).isNull();
    }

    @Test
    void shouldSaveEventWithTicketReservations() {
        Event event = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");

        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Test Client", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignClient(client);
        event.addTicketReservation(reservation);

        entityManager.persistAndFlush(event);
        entityManager.clear();

        Event foundEvent = entityManager.find(Event.class, event.getId());

        assertThat(foundEvent.getTicketReservations()).hasSize(1);
        assertThat(foundEvent.getTicketReservations().get(0).getNumberOfTickets()).isEqualTo(2);
        assertThat(foundEvent.getTicketReservations().get(0).getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void shouldCascadeRemoveEventWithReservations() {
        Event event = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");

        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Test Client", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignClient(client);
        event.addTicketReservation(reservation);

        entityManager.persistAndFlush(event);
        Long eventId = event.getId();
        Long reservationId = event.getTicketReservations().get(0).getId();

        entityManager.remove(event);
        entityManager.flush();

        assertThat(entityManager.find(Event.class, eventId)).isNull();
        assertThat(entityManager.find(TicketReservation.class, reservationId)).isNull();
    }

    @Test
    void shouldFindEventByName() {
        Event event = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");

        entityManager.persistAndFlush(event);
        entityManager.clear();

        Event foundEvent = entityManager.getEntityManager()
                .createQuery("SELECT e FROM Event e WHERE e.name = :name", Event.class)
                .setParameter("name", "Concert")
                .getSingleResult();

        assertThat(foundEvent).isNotNull();
        assertThat(foundEvent.getTicketPrice().compareTo(BigDecimal.valueOf(100))).isEqualTo(0);
    }

    @Test
    void shouldFindEventsByStatus() {
        Event event1 = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Desc1");
        Event event2 = new Event("Theater", LocalDate.now().plusDays(5),
                BigDecimal.valueOf(50), EventStatus.CANCELED, "Desc2");

        entityManager.persistAndFlush(event1);
        entityManager.persistAndFlush(event2);
        entityManager.clear();

        List<Event> plannedEvents = entityManager.getEntityManager()
                .createQuery("SELECT e FROM Event e WHERE e.status = :status", Event.class)
                .setParameter("status", EventStatus.PLANNED)
                .getResultList();

        assertThat(plannedEvents).hasSize(1);
        assertThat(plannedEvents.get(0).getName()).isEqualTo("Concert");
    }

    @Test
    void shouldFindEventsByDateRange() {
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(15);

        Event event1 = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Desc1");
        Event event2 = new Event("Theater", LocalDate.now().plusDays(20),
                BigDecimal.valueOf(50), EventStatus.PLANNED, "Desc2");

        entityManager.persistAndFlush(event1);
        entityManager.persistAndFlush(event2);
        entityManager.clear();

        List<Event> eventsInRange = entityManager.getEntityManager()
                .createQuery("SELECT e FROM Event e WHERE e.date BETWEEN :startDate AND :endDate", Event.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();

        assertThat(eventsInRange).hasSize(1);
        assertThat(eventsInRange.get(0).getName()).isEqualTo("Concert");
    }

    @Test
    void shouldHandleEventWithoutReservations() {
        Event event = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");

        entityManager.persistAndFlush(event);

        assertThat(event.getTicketReservations()).isEmpty();
    }

    @Test
    void shouldUpdateEventTimestampOnUpdate() throws InterruptedException {
        Event event = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");

        entityManager.persistAndFlush(event);
        LocalDateTime initialUpdatedAt = event.getUpdatedAt();

        Thread.sleep(1);
        event.setName("Updated Concert Name");
        Event updatedEvent = entityManager.persistAndFlush(event);

        assertThat(updatedEvent.getUpdatedAt()).isAfter(initialUpdatedAt);
    }

    @Test
    void shouldMaintainBidirectionalRelationshipAfterPersist() {
        Event event = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");

        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Test Client", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignClient(client);
        event.addTicketReservation(reservation);

        entityManager.persistAndFlush(event);
        entityManager.clear();

        Event foundEvent = entityManager.find(Event.class, event.getId());
        TicketReservation foundReservation = foundEvent.getTicketReservations().get(0);

        assertThat(foundReservation.getEvent()).isEqualTo(foundEvent);
    }
}