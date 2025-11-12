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

        Event savedEvent = entityManager.persistAndFlush(event);

        assertThat(savedEvent.getId()).isNotNull();
        assertThat(savedEvent.getName()).isEqualTo("Concert");
        assertThat(savedEvent.getDate()).isAfter(LocalDate.now());
        assertThat(savedEvent.getTicketPrice()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.PLANNED);
        assertThat(savedEvent.getDescription()).isEqualTo("Music concert");
        assertThat(savedEvent.getCreatedAt()).isNotNull();
        assertThat(savedEvent.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldSetDefaultValuesOnPersist() {
        Event event = new Event();
        event.setName("Test Event");
        event.setDate(LocalDate.now().plusDays(5));

        Event savedEvent = entityManager.persistAndFlush(event);

        assertThat(savedEvent.getTicketPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.PLANNED);
        assertThat(savedEvent.getCreatedAt()).isNotNull();
        assertThat(savedEvent.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldEnforceUniqueNameAndDateConstraint() {
        LocalDate sameDate = LocalDate.now().plusDays(10);
        Event event1 = new Event("Concert", sameDate, BigDecimal.valueOf(100), EventStatus.PLANNED, "Desc1");
        Event event2 = new Event("Concert", sameDate, BigDecimal.valueOf(150), EventStatus.CANCELLED, "Desc2");

        entityManager.persistAndFlush(event1);

        assertThatThrownBy(() -> entityManager.persistAndFlush(event2))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldAllowSameNameWithDifferentDates() {
        Event event1 = new Event("Concert", LocalDate.now().plusDays(10), BigDecimal.valueOf(100), EventStatus.PLANNED, "Desc1");
        Event event2 = new Event("Concert", LocalDate.now().plusDays(11), BigDecimal.valueOf(150), EventStatus.PLANNED, "Desc2");

        Event savedEvent1 = entityManager.persistAndFlush(event1);
        Event savedEvent2 = entityManager.persistAndFlush(event2);

        assertThat(savedEvent1.getId()).isNotNull();
        assertThat(savedEvent2.getId()).isNotNull();
        assertThat(savedEvent1.getName()).isEqualTo(savedEvent2.getName());
        assertThat(savedEvent1.getDate()).isNotEqualTo(savedEvent2.getDate());
    }

    @Test
    void shouldUpdateEventFields() {
        Event event = new Event("Old Name", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Old Desc");
        Event savedEvent = entityManager.persistAndFlush(event);

        savedEvent.setName("New Name");
        savedEvent.setTicketPrice(BigDecimal.valueOf(200));
        savedEvent.setStatus(EventStatus.CANCELLED);
        savedEvent.setDescription("New Description");

        Event updatedEvent = entityManager.persistAndFlush(savedEvent);

        assertThat(updatedEvent.getName()).isEqualTo("New Name");
        assertThat(updatedEvent.getTicketPrice()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(updatedEvent.getStatus()).isEqualTo(EventStatus.CANCELLED);
        assertThat(updatedEvent.getDescription()).isEqualTo("New Description");
        assertThat(updatedEvent.getUpdatedAt()).isAfter(updatedEvent.getCreatedAt());
    }

    @Test
    void shouldSaveEventWithNullDescription() {
        Event event = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, null);

        Event savedEvent = entityManager.persistAndFlush(event);

        assertThat(savedEvent.getId()).isNotNull();
        assertThat(savedEvent.getDescription()).isNull();
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

        Event savedEvent = entityManager.persistAndFlush(event);
        entityManager.clear();

        Event foundEvent = entityManager.find(Event.class, savedEvent.getId());

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

        Event savedEvent = entityManager.persistAndFlush(event);
        Long eventId = savedEvent.getId();
        Long reservationId = savedEvent.getTicketReservations().get(0).getId();

        entityManager.remove(savedEvent);
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
                BigDecimal.valueOf(50), EventStatus.CANCELLED, "Desc2");

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

        Event savedEvent = entityManager.persistAndFlush(event);

        assertThat(savedEvent.getTicketReservations()).isEmpty();
    }

    @Test
    void shouldUpdateEventTimestampOnUpdate() throws InterruptedException {
        Event event = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");

        Event savedEvent = entityManager.persistAndFlush(event);
        LocalDateTime initialUpdatedAt = savedEvent.getUpdatedAt();

        Thread.sleep(1);
        savedEvent.setName("Updated Concert Name");
        Event updatedEvent = entityManager.persistAndFlush(savedEvent);

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

        Event savedEvent = entityManager.persistAndFlush(event);
        entityManager.clear();

        Event foundEvent = entityManager.find(Event.class, savedEvent.getId());
        TicketReservation foundReservation = foundEvent.getTicketReservations().get(0);

        assertThat(foundReservation.getEvent()).isEqualTo(foundEvent);
    }
}