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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TicketReservationEntityIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveTicketReservationWithClientAndEvent() {
        // Сначала сохраняем паспорт, клиента и событие
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        Event event = new Event("Концерт", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        entityManager.persistAndFlush(event);

        // Затем создаем и сохраняем резервацию
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignClient(client);
        reservation.assignEvent(event);

        TicketReservation savedReservation = entityManager.persistAndFlush(reservation);

        assertThat(savedReservation.getId()).isNotNull();
        assertThat(savedReservation.getNumberOfTickets()).isEqualTo(2);
        assertThat(savedReservation.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(savedReservation.getCreatedAt()).isNotNull();
        assertThat(savedReservation.getUpdatedAt()).isNotNull();
        assertThat(savedReservation.getClient().getId()).isEqualTo(client.getId());
        assertThat(savedReservation.getEvent().getId()).isEqualTo(event.getId());
    }

    @Test
    void shouldSetDefaultValuesOnPersist() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        Event event = new Event("Концерт", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        entityManager.persistAndFlush(event);

        TicketReservation reservation = new TicketReservation();
        reservation.assignClient(client);
        reservation.assignEvent(event);

        TicketReservation savedReservation = entityManager.persistAndFlush(reservation);

        assertThat(savedReservation.getNumberOfTickets()).isEqualTo(1);
        assertThat(savedReservation.getBookingStatus()).isEqualTo(BookingStatus.PENDING_CONFIRMATION);
        assertThat(savedReservation.getCreatedAt()).isNotNull();
        assertThat(savedReservation.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateReservationFields() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        Event event = new Event("Концерт", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        entityManager.persistAndFlush(event);

        TicketReservation reservation = new TicketReservation(2, BookingStatus.PENDING_CONFIRMATION);
        reservation.assignClient(client);
        reservation.assignEvent(event);

        TicketReservation savedReservation = entityManager.persistAndFlush(reservation);

        savedReservation.setNumberOfTickets(5);
        savedReservation.setBookingStatus(BookingStatus.CONFIRMED);

        TicketReservation updatedReservation = entityManager.persistAndFlush(savedReservation);

        assertThat(updatedReservation.getNumberOfTickets()).isEqualTo(5);
        assertThat(updatedReservation.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(updatedReservation.getUpdatedAt()).isAfter(updatedReservation.getCreatedAt());
    }

    @Test
    void shouldCascadeFromClientAndEvent() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        Event event = new Event("Концерт", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        entityManager.persistAndFlush(event);

        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignClient(client);
        reservation.assignEvent(event);

        // Сохраняем резервацию в БД
        entityManager.persistAndFlush(reservation);

        Long reservationId = reservation.getId();
        Long clientId = client.getId();

        // Очищаем контекст и перезагружаем объекты
        entityManager.clear();
        Client clientToDelete = entityManager.find(Client.class, clientId);

        // При удалении клиента резервация должна удалиться (каскад ALL от Client)
        entityManager.remove(clientToDelete);
        entityManager.flush();

        assertThat(entityManager.find(TicketReservation.class, reservationId)).isNull();
    }

    @Test
    void shouldMaintainBidirectionalRelationships() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        Event event = new Event("Концерт", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        entityManager.persistAndFlush(event);

        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignClient(client);
        reservation.assignEvent(event);

        TicketReservation savedReservation = entityManager.persistAndFlush(reservation);
        entityManager.clear();

        TicketReservation foundReservation = entityManager.find(TicketReservation.class, savedReservation.getId());

        assertThat(foundReservation.getClient()).isNotNull();
        assertThat(foundReservation.getEvent()).isNotNull();
        assertThat(foundReservation.getClient().getTicketReservations()).isNotEmpty();
        assertThat(foundReservation.getEvent().getTicketReservations()).isNotEmpty();
    }

    @Test
    void shouldFindReservationsByClient() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        Event event = new Event("Концерт", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        entityManager.persistAndFlush(event);

        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignClient(client);
        reservation.assignEvent(event);

        entityManager.persistAndFlush(reservation);
        entityManager.clear();

        var foundReservations = entityManager.getEntityManager()
                .createQuery("SELECT tr FROM TicketReservation tr WHERE tr.client.id = :clientId", TicketReservation.class)
                .setParameter("clientId", client.getId())
                .getResultList();

        assertThat(foundReservations).hasSize(1);
        assertThat(foundReservations.get(0).getNumberOfTickets()).isEqualTo(2);
    }

    @Test
    void shouldFindReservationsByEvent() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        Event event = new Event("Концерт", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        entityManager.persistAndFlush(event);

        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignClient(client);
        reservation.assignEvent(event);

        entityManager.persistAndFlush(reservation);
        entityManager.clear();

        var foundReservations = entityManager.getEntityManager()
                .createQuery("SELECT tr FROM TicketReservation tr WHERE tr.event.id = :eventId", TicketReservation.class)
                .setParameter("eventId", event.getId())
                .getResultList();

        assertThat(foundReservations).hasSize(1);
        assertThat(foundReservations.get(0).getNumberOfTickets()).isEqualTo(2);
    }

    @Test
    void shouldFindReservationsByStatus() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        Event event = new Event("Концерт", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        entityManager.persistAndFlush(event);

        TicketReservation reservation1 = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation1.assignClient(client);
        reservation1.assignEvent(event);

        TicketReservation reservation2 = new TicketReservation(1, BookingStatus.PENDING_CONFIRMATION);
        reservation2.assignClient(client);
        reservation2.assignEvent(event);

        entityManager.persistAndFlush(reservation1);
        entityManager.persistAndFlush(reservation2);
        entityManager.clear();

        var confirmedReservations = entityManager.getEntityManager()
                .createQuery("SELECT tr FROM TicketReservation tr WHERE tr.bookingStatus = :status", TicketReservation.class)
                .setParameter("status", BookingStatus.CONFIRMED)
                .getResultList();

        assertThat(confirmedReservations).hasSize(1);
        assertThat(confirmedReservations.get(0).getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void shouldUpdateTimestampOnUpdate() throws InterruptedException {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        Event event = new Event("Концерт", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        entityManager.persistAndFlush(event);

        TicketReservation reservation = new TicketReservation(2, BookingStatus.PENDING_CONFIRMATION);
        reservation.assignClient(client);
        reservation.assignEvent(event);

        TicketReservation savedReservation = entityManager.persistAndFlush(reservation);
        LocalDateTime initialUpdatedAt = savedReservation.getUpdatedAt();

        Thread.sleep(1);
        savedReservation.setNumberOfTickets(3);
        TicketReservation updatedReservation = entityManager.persistAndFlush(savedReservation);

        assertThat(updatedReservation.getUpdatedAt()).isAfter(initialUpdatedAt);
    }

    @Test
    void shouldHandleMultipleReservationsForSameClientAndEvent() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        Event event = new Event("Концерт", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        entityManager.persistAndFlush(event);

        TicketReservation reservation1 = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation1.assignClient(client);
        reservation1.assignEvent(event);

        TicketReservation reservation2 = new TicketReservation(3, BookingStatus.PENDING_CONFIRMATION);
        reservation2.assignClient(client);
        reservation2.assignEvent(event);

        TicketReservation saved1 = entityManager.persistAndFlush(reservation1);
        TicketReservation saved2 = entityManager.persistAndFlush(reservation2);

        assertThat(saved1.getId()).isNotNull();
        assertThat(saved2.getId()).isNotNull();
        assertThat(saved1.getClient()).isEqualTo(saved2.getClient());
        assertThat(saved1.getEvent()).isEqualTo(saved2.getEvent());
    }
}