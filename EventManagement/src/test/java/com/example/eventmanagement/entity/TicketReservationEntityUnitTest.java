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

import static org.assertj.core.api.Assertions.assertThat;

class TicketReservationEntityUnitTest {

    private Client client;
    private Event event;

    @BeforeEach
    void setUp() {
        Passport passport = new Passport("1234", "567890");
        client = new Client("Test Client", "+79123456789", "test@mail.com", passport);
        event = new Event("Test Event", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");
    }

    @Test
    void shouldSetDefaultValuesOnCreate() {
        TicketReservation reservation = new TicketReservation();
        reservation.onCreate();

        assertThat(reservation.getNumberOfTickets()).isEqualTo(1);
        assertThat(reservation.getBookingStatus()).isEqualTo(BookingStatus.PENDING_CONFIRMATION);
        assertThat(reservation.getCreatedAt()).isNotNull();
        assertThat(reservation.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateTimestampOnUpdate() throws InterruptedException {
        TicketReservation reservation = new TicketReservation();
        reservation.onCreate();
        LocalDateTime initialUpdate = reservation.getUpdatedAt();

        Thread.sleep(1);
        reservation.onUpdate();

        assertThat(reservation.getUpdatedAt()).isAfter(initialUpdate);
    }

    @Test
    void shouldAssignClient() {
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);

        reservation.assignClient(client);

        assertThat(reservation.getClient()).isEqualTo(client);
    }

    @Test
    void shouldAssignEvent() {
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);

        reservation.assignEvent(event);

        assertThat(reservation.getEvent()).isEqualTo(event);
    }

    @Test
    void shouldCreateReservationWithNumberOfTicketsAndStatus() {
        TicketReservation reservation = new TicketReservation(3, BookingStatus.CONFIRMED);

        assertThat(reservation.getNumberOfTickets()).isEqualTo(3);
        assertThat(reservation.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void shouldUpdateNumberOfTickets() {
        TicketReservation reservation = new TicketReservation(2, BookingStatus.PENDING_CONFIRMATION);

        reservation.setNumberOfTickets(5);

        assertThat(reservation.getNumberOfTickets()).isEqualTo(5);
    }

    @Test
    void shouldUpdateBookingStatus() {
        TicketReservation reservation = new TicketReservation(2, BookingStatus.PENDING_CONFIRMATION);

        reservation.setBookingStatus(BookingStatus.CONFIRMED);

        assertThat(reservation.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void shouldMaintainEqualsAndHashCodeConsistency() {
        TicketReservation reservation1 = new TicketReservation(2, BookingStatus.CONFIRMED);
        TicketReservation reservation2 = new TicketReservation(2, BookingStatus.CONFIRMED);
        TicketReservation reservation3 = new TicketReservation(1, BookingStatus.PENDING_CONFIRMATION);

        reservation1.setId(1L);
        reservation2.setId(1L);
        reservation3.setId(2L);

        assertThat(reservation1).isEqualTo(reservation2);
        assertThat(reservation1).isNotEqualTo(reservation3);
        assertThat(reservation1.hashCode()).isEqualTo(reservation2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithNull() {
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.setId(1L);

        assertThat(reservation).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualWithDifferentClass() {
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.setId(1L);

        assertThat(reservation).isNotEqualTo("not a reservation");
    }

    @Test
    void toStringShouldContainImportantFields() {
        TicketReservation reservation = new TicketReservation(3, BookingStatus.CONFIRMED);
        reservation.setId(1L);
        reservation.onCreate();

        String toString = reservation.toString();

        assertThat(toString).contains("id=1");
        assertThat(toString).contains("numberOfTickets=3");
        assertThat(toString).contains("bookingStatus='CONFIRMED'");
        assertThat(toString).contains("createdAt=");
        assertThat(toString).contains("updatedAt=");
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1, true",
            "1, 2, false",
            "2, 1, false"
    })
    void testEqualsWithVariousIds(Long id1, Long id2, boolean expectedEqual) {
        TicketReservation reservation1 = new TicketReservation();
        TicketReservation reservation2 = new TicketReservation();

        reservation1.setId(id1);
        reservation2.setId(id2);

        if (expectedEqual) {
            assertThat(reservation1).isEqualTo(reservation2);
        } else {
            assertThat(reservation1).isNotEqualTo(reservation2);
        }
    }

    @Test
    void shouldHandleNullClientAndEvent() {
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);

        reservation.assignClient(null);
        reservation.assignEvent(null);

        assertThat(reservation.getClient()).isNull();
        assertThat(reservation.getEvent()).isNull();
    }
}