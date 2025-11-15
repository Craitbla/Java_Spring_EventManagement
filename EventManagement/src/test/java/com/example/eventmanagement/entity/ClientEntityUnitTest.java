package com.example.eventmanagement.entity;

import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.entity.TicketReservation;
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

class ClientEntityUnitTest {

    private Passport defaultPassport;

    @BeforeEach
    void setUp() {
        defaultPassport = new Passport("1234", "567890");
    }

    @Test
    void shouldSetTimestampsOnCreate() {
        Client client = new Client();
        client.onCreate();

        assertThat(client.getCreatedAt()).isNotNull();
        assertThat(client.getUpdatedAt()).isNotNull();
        assertThat(client.getCreatedAt()).isBetween(client.getUpdatedAt().minusSeconds(1), client.getUpdatedAt().plusSeconds(1));
    }

    @Test
    void shouldUpdateTimestampOnUpdate() throws InterruptedException {
        Client client = new Client();
        client.onCreate();
        LocalDateTime initialUpdate = client.getUpdatedAt();

        Thread.sleep(1);
        client.onUpdate();

        assertThat(client.getUpdatedAt()).isAfter(initialUpdate);
    }

    @Test
    void shouldSetPassportBidirectionalRelationship() {
        Client client = new Client();
        Passport passport = new Passport("1234", "567890");

        client.setPassport(passport);

        assertThat(client.getPassport()).isEqualTo(passport);
        assertThat(passport.getClient()).isEqualTo(client);
    }

    @Test
    void shouldClearOldPassportWhenSettingNewOne() {
        Client client = new Client();
        Passport oldPassport = new Passport("1111", "222222");
        Passport newPassport = new Passport("3333", "444444");

        client.setPassport(oldPassport);
        client.setPassport(newPassport);

        assertThat(client.getPassport()).isEqualTo(newPassport);
        assertThat(oldPassport.getClient()).isNull();
        assertThat(newPassport.getClient()).isEqualTo(client);
    }

    @Test
    void shouldHandleNullPassport() {
        Client client = new Client();
        Passport passport = new Passport("1234", "567890");

        client.setPassport(passport);
        client.setPassport(null);

        assertThat(client.getPassport()).isNull();
        assertThat(passport.getClient()).isNull();
    }

    @Test
    void shouldAddTicketReservation() {
        Client client = new Client();
        Event event = new Event("Concert", LocalDate.now().plusDays(10), BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignEvent(event);

        client.addTicketReservation(reservation);

        assertThat(client.getTicketReservations()).contains(reservation);
        assertThat(reservation.getClient()).isEqualTo(client);
    }

    @Test
    void shouldRemoveTicketReservation() {
        Client client = new Client();
        Event event = new Event("Concert", LocalDate.now().plusDays(10), BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignEvent(event);

        client.addTicketReservation(reservation);
        boolean removed = client.removeTicketReservation(reservation);

        assertThat(removed).isTrue();
        assertThat(client.getTicketReservations()).doesNotContain(reservation);
        assertThat(reservation.getClient()).isNull();
    }

    @Test
    void shouldReturnFalseWhenRemovingNonExistentReservation() {
        Client client = new Client();
        Event event = new Event("Concert", LocalDate.now().plusDays(10), BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignEvent(event);

        boolean removed = client.removeTicketReservation(reservation);

        assertThat(removed).isFalse();
    }

    @Test
    void shouldSetTicketReservationsBidirectional() {
        Client client = new Client();
        Event event1 = new Event("Concert", LocalDate.now().plusDays(10), BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");
        Event event2 = new Event("Theater", LocalDate.now().plusDays(5), BigDecimal.valueOf(50), EventStatus.PLANNED, "Play");

        List<TicketReservation> reservations = List.of(
                new TicketReservation(1, BookingStatus.PENDING_CONFIRMATION),
                new TicketReservation(3, BookingStatus.CONFIRMED)
        );
        reservations.get(0).assignEvent(event1);
        reservations.get(1).assignEvent(event2);

        client.setTicketReservations(reservations);

        assertThat(client.getTicketReservations()).hasSize(2);
        assertThat(reservations.get(0).getClient()).isEqualTo(client);
        assertThat(reservations.get(1).getClient()).isEqualTo(client);
    }

    @Test
    void shouldClearOldReservationsWhenSettingNewOnes() {
        Client client = new Client();
        Event event = new Event("Concert", LocalDate.now().plusDays(10), BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");

        TicketReservation oldReservation = new TicketReservation(1, BookingStatus.PENDING_CONFIRMATION);
        oldReservation.assignEvent(event);
        client.addTicketReservation(oldReservation);

        TicketReservation newReservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        newReservation.assignEvent(event);
        client.setTicketReservations(List.of(newReservation));

        assertThat(client.getTicketReservations()).containsOnly(newReservation);
        assertThat(oldReservation.getClient()).isNull();
        assertThat(newReservation.getClient()).isEqualTo(client);
    }

    @Test
    void shouldHandleNullReservationsList() {
        Client client = new Client();
        Event event = new Event("Concert", LocalDate.now().plusDays(10), BigDecimal.valueOf(100), EventStatus.PLANNED, "Description");

        TicketReservation reservation = new TicketReservation(1, BookingStatus.PENDING_CONFIRMATION);
        reservation.assignEvent(event);
        client.addTicketReservation(reservation);

        client.setTicketReservations(null);

        assertThat(client.getTicketReservations()).isEmpty();
        assertThat(reservation.getClient()).isNull();
    }

    @Test
    void shouldMaintainEqualsAndHashCodeConsistency() {
        Client client1 = new Client("Иванов Иван", "+79123456789", defaultPassport);
        Client client2 = new Client("Петров Петр", "+79223456789", defaultPassport);

        client1.setId(1L);
        client2.setId(1L);

        assertThat(client1).isEqualTo(client2);
        assertThat(client1.hashCode()).isEqualTo(client2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithDifferentIds() {
        Client client1 = new Client("Иванов Иван", "+79123456789", defaultPassport);
        Client client2 = new Client("Петров Петр", "+79223456789", defaultPassport);

        client1.setId(1L);
        client2.setId(2L);

        assertThat(client1).isNotEqualTo(client2);
    }

    @Test
    void shouldNotBeEqualWithNull() {
        Client client = new Client("Иванов Иван", "+79123456789", defaultPassport);
        client.setId(1L);

        assertThat(client).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualWithDifferentClass() {
        Client client = new Client("Иванов Иван", "+79123456789", defaultPassport);
        client.setId(1L);

        assertThat(client).isNotEqualTo("not a client");
    }

    @Test
    void toStringShouldContainImportantFields() {
        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", defaultPassport);
        client.setId(1L);

        String toString = client.toString();

        assertThat(toString).contains("id=1");
        assertThat(toString).contains("fullName='Иванов Иван'");
        assertThat(toString).contains("phoneNumber='+79123456789'");
        assertThat(toString).contains("email='test@mail.com'");
        assertThat(toString).contains("createdAt=");
        assertThat(toString).contains("updatedAt=");
    }

    @Test
    void shouldCreateClientWithRequiredFieldsOnly() {
        Client client = new Client("Иванов Иван", "+79123456789", defaultPassport);

        assertThat(client.getFullName()).isEqualTo("Иванов Иван");
        assertThat(client.getPhoneNumber()).isEqualTo("+79123456789");
        assertThat(client.getPassport()).isEqualTo(defaultPassport);
        assertThat(client.getEmail()).isNull();
    }

    @Test
    void shouldCreateClientWithAllFields() {
        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", defaultPassport);

        assertThat(client.getFullName()).isEqualTo("Иванов Иван");
        assertThat(client.getPhoneNumber()).isEqualTo("+79123456789");
        assertThat(client.getEmail()).isEqualTo("test@mail.com");
        assertThat(client.getPassport()).isEqualTo(defaultPassport);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1, true",
            "1, 2, false",
            "2, 1, false"
    })
    void testEqualsWithVariousIds(Long id1, Long id2, boolean expectedEqual) {
        Client client1 = new Client();
        Client client2 = new Client();

        client1.setId(id1);
        client2.setId(id2);

        if (expectedEqual) {
            assertThat(client1).isEqualTo(client2);
        } else {
            assertThat(client1).isNotEqualTo(client2);
        }
    }
}