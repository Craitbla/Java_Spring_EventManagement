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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class ClientEntityIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveClientWithPassport() {
        // Сначала сохраняем паспорт отдельно
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);

        entityManager.persistAndFlush(client);

        assertThat(client.getId()).isNotNull();
        assertThat(client.getFullName()).isEqualTo("Иванов Иван");
        assertThat(client.getPhoneNumber()).isEqualTo("+79123456789");
        assertThat(client.getEmail()).isEqualTo("test@mail.com");
        assertThat(client.getCreatedAt()).isNotNull();
        assertThat(client.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCascadeSavePassportWhenSavingClient() {
        Passport passport = new Passport("1234", "567890");

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);

        entityManager.persistAndFlush(client);
Client findedClient =  entityManager.find(Client.class, client.getId());
        Passport findedPassport =  entityManager.find(Passport.class, passport.getId());
        assertThat(findedClient).isEqualTo(client);
        assertThat(findedPassport).isEqualTo(passport);
    }

    @Test
    void shouldEnforceUniquePhoneNumber() {
        Passport passport1 = new Passport("1234", "567890");
        Passport passport2 = new Passport("5678", "901234");
        entityManager.persistAndFlush(passport1);
        entityManager.persistAndFlush(passport2);

        Client client1 = new Client("Иванов Иван", "+79123456789", "test1@mail.com", passport1);
        Client client2 = new Client("Петров Петр", "+79123456789", "test2@mail.com", passport2);

        entityManager.persistAndFlush(client1);

        assertThatThrownBy(() -> entityManager.persistAndFlush(client2))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldEnforceUniqueEmail() {
        Passport passport1 = new Passport("1234", "567890");
        Passport passport2 = new Passport("5678", "901234");
        entityManager.persistAndFlush(passport1);
        entityManager.persistAndFlush(passport2);

        Client client1 = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport1);
        Client client2 = new Client("Петров Петр", "+79223456789", "test@mail.com", passport2);

        entityManager.persistAndFlush(client1);

        assertThatThrownBy(() -> entityManager.persistAndFlush(client2))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldEnforceUniquePassport() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client1 = new Client("Иванов Иван", "+79123456789", "test1@mail.com", passport);
        Client client2 = new Client("Петров Петр", "+79223456789", "test2@mail.com", passport);

        entityManager.persistAndFlush(client1);

        assertThatThrownBy(() -> entityManager.persistAndFlush(client2))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldSaveClientWithNullEmail() {/////////////////////////////////переделка
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", passport);

        entityManager.persistAndFlush(client);

        assertThat(client.getId()).isNotNull();
        assertThat(client.getEmail()).isNull();
    }

    @Test
    void shouldUpdateClientFields() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "old@mail.com", passport);
        entityManager.persistAndFlush(client);

        client.setFullName("Петров Петр");
        client.setEmail("new@mail.com");
        client.setPhoneNumber("+79223456789");

        Client updatedClient = entityManager.persistAndFlush(client);

        assertThat(updatedClient.getFullName()).isEqualTo("Петров Петр");
        assertThat(updatedClient.getEmail()).isEqualTo("new@mail.com");
        assertThat(updatedClient.getPhoneNumber()).isEqualTo("+79223456789");
        assertThat(updatedClient.getUpdatedAt()).isAfter(updatedClient.getCreatedAt());
    }

    @Test
    void shouldUpdatePassportThroughClient() {
        Passport oldPassport = new Passport("1234", "567890");
        entityManager.persistAndFlush(oldPassport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", oldPassport);
        entityManager.persistAndFlush(client);

        Passport newPassport = new Passport("9999", "888888");
        entityManager.persistAndFlush(newPassport);

        client.setPassport(newPassport);

        Client updatedClient = entityManager.persistAndFlush(client);

        assertThat(updatedClient.getPassport().getSeries()).isEqualTo("9999");
        assertThat(updatedClient.getPassport().getNumber()).isEqualTo("888888");
    }

    @Test
    void shouldSaveClientWithTicketReservations() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);

        Event event = new Event("Концерт", LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        entityManager.persistAndFlush(event);

        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.assignEvent(event);
        client.addTicketReservation(reservation);

        entityManager.persistAndFlush(client);
        entityManager.clear();

        Client foundClient = entityManager.find(Client.class, client.getId());

        assertThat(foundClient.getTicketReservations()).hasSize(1);
        assertThat(foundClient.getTicketReservations().get(0).getNumberOfTickets()).isEqualTo(2);
        assertThat(foundClient.getTicketReservations().get(0).getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }


    @Test
    void shouldFindClientByPhoneNumber() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);

        entityManager.persistAndFlush(client);
        entityManager.clear();

        Client foundClient = entityManager.getEntityManager()
                .createQuery("SELECT c FROM Client c WHERE c.phoneNumber = :phone", Client.class)
                .setParameter("phone", "+79123456789")
                .getSingleResult();

        assertThat(foundClient).isNotNull();
        assertThat(foundClient.getFullName()).isEqualTo("Иванов Иван");
    }

    @Test
    void shouldMaintainBidirectionalRelationshipAfterPersist() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);

        entityManager.persistAndFlush(client);
        entityManager.clear();

        Client foundClient = entityManager.find(Client.class, client.getId());
        Passport foundPassport = foundClient.getPassport();

        assertThat(foundPassport.getClient()).isEqualTo(foundClient);
    }

    @Test
    void shouldHandleClientWithoutReservations() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);

        entityManager.persistAndFlush(client);

        assertThat(client.getTicketReservations()).isEmpty();
    }

    @Test
    void shouldUpdateClientTimestampOnUpdate() throws InterruptedException {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);

        entityManager.persistAndFlush(client);
        LocalDateTime initialUpdatedAt = client.getUpdatedAt();

        Thread.sleep(1);
        client.setFullName("Обновленное имя");
        Client updatedClient = entityManager.persistAndFlush(client);

        assertThat(updatedClient.getUpdatedAt()).isAfter(initialUpdatedAt);
    }

}