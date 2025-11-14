package com.example.eventmanagement.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class PassportEntityIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSavePassportWithAllFields() {
        Passport passport = new Passport("1234", "567890");

        entityManager.persistAndFlush(passport);

        assertThat(passport.getId()).isNotNull();
        assertThat(passport.getSeries()).isEqualTo("1234");
        assertThat(passport.getNumber()).isEqualTo("567890");
        assertThat(passport.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldSetTimestampOnPersist() {
        Passport passport = new Passport("1234", "567890");

        entityManager.persistAndFlush(passport);

        assertThat(passport.getCreatedAt()).isNotNull();
        assertThat(passport.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void shouldEnforceUniqueSeriesAndNumberConstraint() {
        Passport passport1 = new Passport("1234", "567890");
        Passport passport2 = new Passport("1234", "567890");

        entityManager.persistAndFlush(passport1);

        assertThatThrownBy(() -> entityManager.persistAndFlush(passport2))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldAllowDifferentSeriesWithSameNumber() {
        Passport passport1 = new Passport("1234", "567890");
        Passport passport2 = new Passport("5678", "567890");

        entityManager.persistAndFlush(passport1);
        entityManager.persistAndFlush(passport2);

        assertThat(passport1.getId()).isNotNull();
        assertThat(passport2.getId()).isNotNull();
        assertThat(passport1.getNumber()).isEqualTo(passport2.getNumber());
        assertThat(passport1.getSeries()).isNotEqualTo(passport2.getSeries());
    }

    @Test
    void shouldAllowSameSeriesWithDifferentNumber() {
        Passport passport1 = new Passport("1234", "567890");
        Passport passport2 = new Passport("1234", "987654");

        entityManager.persistAndFlush(passport1);
        entityManager.persistAndFlush(passport2);

        assertThat(passport1.getId()).isNotNull();
        assertThat(passport2.getId()).isNotNull();
        assertThat(passport1.getSeries()).isEqualTo(passport2.getSeries());
        assertThat(passport1.getNumber()).isNotEqualTo(passport2.getNumber());
    }

    @Test
    void shouldUpdatePassportFields() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        passport.setSeries("9999");
        passport.setNumber("888888");

        Passport updatedPassport = entityManager.persistAndFlush(passport);

        assertThat(updatedPassport.getSeries()).isEqualTo("9999");
        assertThat(updatedPassport.getNumber()).isEqualTo("888888");
    }

    @Test
    void shouldSavePassportWithoutClient() {
        Passport passport = new Passport("1234", "567890");

        entityManager.persistAndFlush(passport);

        assertThat(passport.getId()).isNotNull();
        assertThat(passport.getClient()).isNull();
    }

    @Test
    void shouldSavePassportWithClient() {
        // Сначала сохраняем паспорт
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        // Затем создаем и сохраняем клиента с этим паспортом
        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        entityManager.clear();

        // Проверяем, что связь установлена корректно
        Passport foundPassport = entityManager.find(Passport.class, passport.getId());
        assertThat(foundPassport.getClient()).isNotNull();
        assertThat(foundPassport.getClient().getId()).isEqualTo(client.getId());
    }

    @Test
    void shouldDeletePassportWhenDeletingClient() {
        Passport passport = new Passport("1234", "567890");
        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        Long clientId = client.getId();
        Long passportId = passport.getId();

        // Удаляем клиента - паспорт должен удалиться каскадно
        entityManager.remove(client);
        entityManager.flush();

        // Проверяем, что оба объекта удалены
        assertThat(entityManager.find(Client.class, clientId)).isNull();
        assertThat(entityManager.find(Passport.class, passportId)).isNull();
    }


    @Test
    void shouldFindPassportBySeriesAndNumber() {
        Passport passport = new Passport("1234", "567890");

        entityManager.persistAndFlush(passport);
        entityManager.clear();

        Passport foundPassport = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Passport p WHERE p.series = :series AND p.number = :number", Passport.class)
                .setParameter("series", "1234")
                .setParameter("number", "567890")
                .getSingleResult();

        assertThat(foundPassport).isNotNull();
        assertThat(foundPassport.getSeries()).isEqualTo("1234");
        assertThat(foundPassport.getNumber()).isEqualTo("567890");
    }

    @Test
    void shouldFindPassportsBySeries() {
        Passport passport1 = new Passport("1234", "567890");
        Passport passport2 = new Passport("1234", "987654");
        Passport passport3 = new Passport("5678", "111111");

        entityManager.persistAndFlush(passport1);
        entityManager.persistAndFlush(passport2);
        entityManager.persistAndFlush(passport3);
        entityManager.clear();

        var foundPassports = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Passport p WHERE p.series = :series", Passport.class)
                .setParameter("series", "1234")
                .getResultList();

        assertThat(foundPassports).hasSize(2);
        assertThat(foundPassports).allMatch(p -> p.getSeries().equals("1234"));
    }

    @Test
    void shouldMaintainBidirectionalRelationship() {
        Passport passport = new Passport("1234", "567890");
        entityManager.persistAndFlush(passport);

        Client client = new Client("Иванов Иван", "+79123456789", "test@mail.com", passport);
        entityManager.persistAndFlush(client);

        entityManager.clear();

        Passport foundPassport = entityManager.find(Passport.class, passport.getId());
        Client foundClient = foundPassport.getClient();

        assertThat(foundClient).isNotNull();
        assertThat(foundClient.getPassport()).isEqualTo(foundPassport);
    }

    @Test
    void shouldHandleMultiplePassportsWithoutClients() {
        Passport passport1 = new Passport("1111", "222222");
        Passport passport2 = new Passport("3333", "444444");
        Passport passport3 = new Passport("5555", "666666");

        Passport saved1 = entityManager.persistAndFlush(passport1);
        Passport saved2 = entityManager.persistAndFlush(passport2);
        Passport saved3 = entityManager.persistAndFlush(passport3);

        assertThat(saved1.getId()).isNotNull();
        assertThat(saved2.getId()).isNotNull();
        assertThat(saved3.getId()).isNotNull();
        assertThat(saved1.getClient()).isNull();
        assertThat(saved2.getClient()).isNull();
        assertThat(saved3.getClient()).isNull();
    }

    @Test
    void shouldNotAllowNullSeries() {
        Passport passport = new Passport();
        passport.setNumber("567890");

        assertThatThrownBy(() -> entityManager.persistAndFlush(passport))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldNotAllowNullNumber() {
        Passport passport = new Passport();
        passport.setSeries("1234");

        assertThatThrownBy(() -> entityManager.persistAndFlush(passport))
                .isInstanceOf(Exception.class);
    }
}