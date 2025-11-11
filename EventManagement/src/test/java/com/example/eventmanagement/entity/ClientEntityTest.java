package com.example.eventmanagement.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class ClientEntityTest {
    @Autowired
    private TestEntityManager entityManager;
    private static final String validFullName = "Иванова Карина Олеговна";
    private static final String validPhone = "+79123456789";
    private static final String validEmail = "valid@email.com";
    private Passport passport1;
    private Client client1;
    private Passport passport2;
    private Client client2;
    @Nested
    class ClientUniqueTest {

        @BeforeEach
        void setUp() {
            // Подготавливаем данные для каждого теста
            passport1 = new Passport("1111", "222222");
            client1 = new Client("Иван Иванов", "+79123456789", "ivan@example.com", passport1);

            passport2 = new Passport("3333", "444444");
            client2 = new Client("Петр Петров", "+79231636131", "petr@example.com", passport2);

            // Сохраняем первого клиента (он будет в БД для всех тестов)
            entityManager.persist(passport1);
            entityManager.persist(client1);
            entityManager.flush();
        }

//        @Test
//        void whenSaveClientsWithSamePhoneNumber_thenThrowException() {
//            // given - первый клиент уже сохранен в @BeforeEach
//            // given - сохраняем паспорт второго клиента
//            entityManager.persist(passport2);
//            client2.setPhoneNumber(client1.getPhoneNumber());
//
//            // when & then - пытаемся сохранить второго клиента с тем же телефоном
//            assertThatThrownBy(() -> {
//                entityManager.persist(client2);
//                entityManager.flush();
//            }).isInstanceOf(RuntimeException.class)
//                    .hasMessageContaining("PHONE_NUMBER");
//        }
//
//        @Test
//        void whenSaveClientsWithSameEmail_thenThrowException() {
//            entityManager.persist(passport2);
//            client2.setEmail(client1.getEmail());
//
//            assertThatThrownBy(() -> {
//                entityManager.persist(client2);
//                entityManager.flush();
//            }).isInstanceOf(RuntimeException.class)
//                    .hasMessageContaining("EMAIL");
//        }
//        @Test
//        void whenSaveClientsWithSamePassport_thenThrowException() {
//            entityManager.persist(passport2);
//            client2.setPassport(client1.getPassport());
//
//            assertThatThrownBy(() -> {
//                entityManager.persist(client2);
//                entityManager.flush();
//            }).isInstanceOf(RuntimeException.class)
//                    .hasMessageContaining("PASSPORT");
//        }
    }
    @Nested
    class ClientRelationshipTest {

        @BeforeEach
        void setUp() {
            // Подготавливаем данные для каждого теста
            passport1 = new Passport("1111", "222222");
            client1 = new Client("Иван Иванов", "+79123456789", "ivan@example.com", passport1);
//            passport2 = new Passport("3333", "444444");
//            client2 = new Client("Петр Петров", "+79231636131", "petr@example.com", passport2);

        }
        @Test
        void whenSaveClientWithPassportCheckBD_thenRelationshipIsEstablished() {

            entityManager.persist(passport1);
            entityManager.persist(client1);
            entityManager.flush();
            entityManager.clear();

            Client foundClient = entityManager.find(Client.class, client1.getId());

            assertThat(foundClient).isNotNull();
            assertThat(foundClient.getPassport()).isNotNull();
            assertThat(foundClient.getPassport().getId()).isEqualTo(passport1.getId());
            assertThat(foundClient.getPassport().getSeries()).isEqualTo(passport1.getSeries());
            assertThat(foundClient.getPassport().getNumber()).isEqualTo(passport1.getNumber());
        }
        @Test
        void whenSaveClientWithPassportCheckCache_thenRelationshipIsEstablished() {

            entityManager.persist(passport1);
            entityManager.persist(client1);
            entityManager.flush();

            Client foundClient = entityManager.find(Client.class, client1.getId());

            assertThat(foundClient).isNotNull();
            assertThat(foundClient.getPassport()).isNotNull();
            assertThat(foundClient.getPassport().getId()).isEqualTo(passport1.getId());
            assertThat(foundClient.getPassport().getSeries()).isEqualTo(passport1.getSeries());
            assertThat(foundClient.getPassport().getNumber()).isEqualTo(passport1.getNumber());
        }
        @Test
        void whenSaveClientWithNewPassport_thenRelationshipIsEstablished() {
            entityManager.persist(passport1);
            entityManager.persist(client1);
            entityManager.flush();

            passport2 = new Passport("3333", "444444");
            client1.setPassport(passport2);
            entityManager.persist(passport2);
            entityManager.persist(client1);
            entityManager.flush();
            entityManager.clear();

            Client foundClient = entityManager.find(Client.class, client1.getId());

            assertThat(foundClient).isNotNull();//проверка что
            assertThat(foundClient.getPassport()).isNotNull();
            assertThat(foundClient.getPassport().getId()).isEqualTo(passport2.getId());
            assertThat(foundClient.getPassport().getSeries()).isEqualTo(passport2.getSeries());
            assertThat(foundClient.getPassport().getNumber()).isEqualTo(passport2.getNumber());
        }
//        @Test////////////////////no
//        void whenSaveClientWithSamePassport_thenRelationshipIsEstablished() {
//            entityManager.persist(passport1);
//            entityManager.persist(client1);
//            entityManager.flush();
//
//            passport2 = new Passport("3333", "444444");
//            client1.setPassport(passport2);
//            entityManager.persist(passport2);
//            entityManager.persist(client1);
//            entityManager.flush();
//            entityManager.clear();
//
//            Client foundClient = entityManager.find(Client.class, client1.getId());
//
//            assertThat(foundClient).isNotNull();//проверка что
//            assertThat(foundClient.getPassport()).isNotNull();
//            assertThat(foundClient.getPassport().getId()).isEqualTo(passport2.getId());
//            assertThat(foundClient.getPassport().getSeries()).isEqualTo(passport2.getSeries());
//            assertThat(foundClient.getPassport().getNumber()).isEqualTo(passport2.getNumber());
//        }
    }



}