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
//    private static final Passport defaultPassport1 = new Passport("1234", "567890");
//    private static final Passport defaultPassport2 = new Passport("2521", "342561");
//    Client createDefaultClient1() {
//        return new Client(validFullName, validPhone, validEmail, defaultPassport1);
//    }
//    Client createDefaultClient2() {
//        return new Client("Сидорова Анна Михайловна", "+79159876543", "sidorova@gmail.com", defaultPassport2);
//    }
private Passport passport1;
    private Client client1;
    private Passport passport2;
    private Client client2;
    @BeforeEach
    void setUp() {
        // Подготавливаем данные для каждого теста
        passport1 = new Passport("1111", "222222");
        client1 = new Client("Иван Иванов", "+79123456789", "ivan@example.com", passport1);

        passport2 = new Passport("3333", "444444");
        client2 = new Client("Петр Петров", "+79123456789", "petr@example.com", passport2);

        // Сохраняем первого клиента (он будет в БД для всех тестов)
        entityManager.persist(passport1);
        entityManager.persist(client1);
        entityManager.flush();
    }
    @Test
    void whenSaveClientsWithSamePhoneNumber_thenThrowException() {
        // given - первый клиент уже сохранен в @BeforeEach
        // given - сохраняем паспорт второго клиента
        entityManager.persist(passport2);

        // when & then - пытаемся сохранить второго клиента с тем же телефоном
        assertThatThrownBy(() -> {
            entityManager.persist(client2);
            entityManager.flush();
        }).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("PHONE_NUMBER");
    }
//
//    @Test
//    void whenSaveClientsWithSamePhoneNumber_thenThrowException() {\
////        Passport passport = new Passport("1234", "567890");
////        entityManager.persist(passport);  // ← ВСЕГДА сначала
////
////        Client client = new Client("Иван", "+79123456789", "ivan@mail.com", passport);
////        entityManager.persist(client);    // ← потом
////        entityManager.flush();
//                Passport passport1 = new Passport("1111", "222222");
//                Client client1 = new Client();
//                client1.setFullName("Иван Иванов");
//                client1.setPhoneNumber("+79123456789");
//                client1.setEmail("ivan@example.com");
//                client1.setPassport(passport1);
////
////                // given - создаем второго клиента с ТЕМ ЖЕ номером телефона
////                Passport passport2 = new Passport("3333", "444444");
////                Client client2 = new Client();
////                client2.setFullName("Петр Петров");
////                client2.setPhoneNumber("+79123456789"); // тот же номер!
////                client2.setEmail("petr@example.com");
////                client2.setPassport(passport2);
////
////                // when - сохраняем первого клиента
//                entityManager.persist(passport1);///////////////////
//                entityManager.flush();
//                entityManager.persist(client1);
//                entityManager.flush();
//////
//////                 then - проверяем, что второй клиент вызывает исключение
//                entityManager.persist(defaultPassport2);
//                entityManager.flush();
//                assertThatThrownBy(() -> {
//                    entityManager.persist(client2);
//                    entityManager.flush(); // исключение будет здесь
//                }).isInstanceOf(RuntimeException.class)
//                        .hasMessageContaining("PHONE_NUMBER");
//
//    }

}