package com.example.eventmanagement.entity;

import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventEntityUnitTest {

    private Event createValidEvent() {
        return new Event("Test Event", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");
    }

    @Test
    void shouldSetDefaultValuesOnCreate() {
        Event event = new Event();
        event.onCreate();

        assertThat(event.getDate()).isNotNull();
        assertThat(event.getTicketPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(event.getStatus()).isEqualTo(EventStatus.PLANNED);
        assertThat(event.getCreatedAt()).isNotNull();
        assertThat(event.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateTimestampOnUpdate() throws InterruptedException {
        Event event = new Event();
        event.onCreate();
        LocalDateTime initialUpdate = event.getUpdatedAt();

        Thread.sleep(1);
        event.onUpdate();

        assertThat(event.getUpdatedAt()).isAfter(initialUpdate);
    }

    @Test
    void shouldAddTicketReservation() {
        Event event = createValidEvent();
        Client client = new Client("Test Client", "+79123456789", new Passport("1234", "567890"));
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignClient(client);

        event.addTicketReservation(reservation);

        assertThat(event.getTicketReservations()).contains(reservation);
        assertThat(reservation.getEvent()).isEqualTo(event);
    }

    @Test
    void shouldRemoveTicketReservation() {
        Event event = createValidEvent();
        Client client = new Client("Test Client", "+79123456789", new Passport("1234", "567890"));
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignClient(client);

        event.addTicketReservation(reservation);
        boolean removed = event.removeTicketReservation(reservation);

        assertThat(removed).isTrue();
        assertThat(event.getTicketReservations()).doesNotContain(reservation);
        assertThat(reservation.getEvent()).isNull();
    }

    @Test
    void shouldReturnFalseWhenRemovingNonExistentReservation() {
        Event event = createValidEvent();
        Client client = new Client("Test Client", "+79123456789", new Passport("1234", "567890"));
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignClient(client);

        boolean removed = event.removeTicketReservation(reservation);

        assertThat(removed).isFalse();
    }

    @Test
    void shouldSetTicketReservationsBidirectional() {
        Event event = createValidEvent();
        Client client1 = new Client("Client 1", "+79123456789", new Passport("1234", "567890"));
        Client client2 = new Client("Client 2", "+79223456789", new Passport("5678", "901234"));

        List<TicketReservation> reservations = List.of(
                new TicketReservation(1, BookingStatus.PENDING_CONFIRMATION),
                new TicketReservation(3, BookingStatus.CONFIRMED)
        );
        reservations.get(0).assignClient(client1);
        reservations.get(1).assignClient(client2);

        event.setTicketReservations(reservations);

        assertThat(event.getTicketReservations()).hasSize(2);
        assertThat(reservations.get(0).getEvent()).isEqualTo(event);
        assertThat(reservations.get(1).getEvent()).isEqualTo(event);
    }

    @Test
    void shouldClearOldReservationsWhenSettingNewOnes() {
        Event event = createValidEvent();
        Client client = new Client("Test Client", "+79123456789", new Passport("1234", "567890"));

        TicketReservation oldReservation = new TicketReservation(1, BookingStatus.PENDING_CONFIRMATION);
        oldReservation.assignClient(client);
        event.addTicketReservation(oldReservation);

        TicketReservation newReservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        newReservation.assignClient(client);
        event.setTicketReservations(List.of(newReservation));

        assertThat(event.getTicketReservations()).containsOnly(newReservation);
        assertThat(oldReservation.getEvent()).isNull();
        assertThat(newReservation.getEvent()).isEqualTo(event);
    }

    @Test
    void shouldHandleNullReservationsList() {
        Event event = createValidEvent();
        Client client = new Client("Test Client", "+79123456789", new Passport("1234", "567890"));

        TicketReservation reservation = new TicketReservation(1, BookingStatus.PENDING_CONFIRMATION);
        reservation.assignClient(client);
        event.addTicketReservation(reservation);

        event.setTicketReservations(null);

        assertThat(event.getTicketReservations()).isEmpty();
        assertThat(reservation.getEvent()).isNull();
    }

    @Test
    void shouldMaintainEqualsAndHashCodeConsistency() {
        Event event1 = new Event("Concert", LocalDate.now().plusDays(10), BigDecimal.valueOf(100), EventStatus.PLANNED, "Desc");
        Event event2 = new Event("Theater", LocalDate.now().plusDays(5), BigDecimal.valueOf(50), EventStatus.CANCELLED, "Play");

        event1.setId(1L);
        event2.setId(1L);

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithDifferentIds() {
        Event event1 = new Event("Concert", LocalDate.now().plusDays(10), BigDecimal.valueOf(100), EventStatus.PLANNED, "Desc");
        Event event2 = new Event("Theater", LocalDate.now().plusDays(5), BigDecimal.valueOf(50), EventStatus.CANCELLED, "Play");

        event1.setId(1L);
        event2.setId(2L);

        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    void shouldNotBeEqualWithNull() {
        Event event = createValidEvent();
        event.setId(1L);

        assertThat(event).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualWithDifferentClass() {
        Event event = createValidEvent();
        event.setId(1L);

        assertThat(event).isNotEqualTo("not an event");
    }

    @Test
    void toStringShouldContainImportantFields() {
        Event event = new Event("Test Event", LocalDate.of(2024, 12, 31),
                BigDecimal.valueOf(150), EventStatus.PLANNED, "Test Description");
        event.setId(1L);

        String toString = event.toString();

        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name='Test Event'");
        assertThat(toString).contains("date=2024-12-31");
        assertThat(toString).contains("ticketPrice=150");
        assertThat(toString).contains("status='PLANNED'");
        assertThat(toString).contains("description='Test Description'");
        assertThat(toString).contains("createdAt=");
        assertThat(toString).contains("updatedAt=");
    }

    @Test
    void shouldCreateEventWithAllFields() {
        Event event = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Music concert");

        assertThat(event.getName()).isEqualTo("Concert");
        assertThat(event.getDate()).isAfter(LocalDate.now());
        assertThat(event.getTicketPrice()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(event.getStatus()).isEqualTo(EventStatus.PLANNED);
        assertThat(event.getDescription()).isEqualTo("Music concert");
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1, true",
            "1, 2, false",
            "2, 1, false"
    })
    void testEqualsWithVariousIds(Long id1, Long id2, boolean expectedEqual) {
        Event event1 = new Event();
        Event event2 = new Event();

        event1.setId(id1);
        event2.setId(id2);

        if (expectedEqual) {
            assertThat(event1).isEqualTo(event2);
        } else {
            assertThat(event1).isNotEqualTo(event2);
        }
    }

    @Test
    void shouldHandleNullDescription() {
        Event event = new Event("Concert", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), EventStatus.PLANNED, null);

        assertThat(event.getDescription()).isNull();
    }
}