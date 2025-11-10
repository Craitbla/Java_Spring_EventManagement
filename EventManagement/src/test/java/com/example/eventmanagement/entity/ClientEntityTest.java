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

import java.util.stream.Stream;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ClientEntityTest {

    @Nested
    class ClientValidationTest {

        private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        private static final Passport defaultPassport = new Passport("1234", "567890");
        private static final String validFullName = "Иванова Карина";
        private static final String validPhone = "+79123456789";
        private static final String validEmail = "valid@email.com";
        static Stream<Arguments> clientDataProvider() {
            return Stream.of(
                    // valid data
                    Arguments.of(new Client(validFullName, validPhone, validEmail, defaultPassport), true, "Валидно"),
                    Arguments.of(new Client("Карина", "+79001112233", "alice@test.ru", defaultPassport), true, "Валидно"),
//
                    // invalid data
                    Arguments.of(new Client(null, validPhone, validEmail, defaultPassport), false, "ФИО не может быть пустым"),
                    Arguments.of(new Client("", validPhone, validEmail, defaultPassport), false, "Имя должно иметь длину между 2 и 100 буквами"),
                    Arguments.of(new Client("И", validPhone, validEmail, defaultPassport), false, "Имя должно иметь длину между 2 и 100 буквами"),
                    Arguments.of(new Client("И".repeat(101), validPhone, validEmail, defaultPassport), false, "Имя должно иметь длину между 2 и 100 буквами"),

                    Arguments.of(new Client(validFullName, "79123456789", validEmail, defaultPassport), false, "Телефон должен начинаться с +7 и иметь 11 цифр"),
                    Arguments.of(new Client(validFullName, "", validEmail, defaultPassport), false, "Телефон должен начинаться с +7 и иметь 11 цифр"),
                    Arguments.of(new Client(validFullName, null, validEmail, defaultPassport), false, "Телефон обязателен"),
                    Arguments.of(new Client(validFullName, validPhone, "invalid-email", defaultPassport), false, "Email должен быть валидным")

            );
        }

        @ParameterizedTest(name = "[{index}] {2}")
        @MethodSource("clientDataProvider")
        void validateClient(Client client, boolean expectedValid, String description) {
            Set<ConstraintViolation<Client>> violations = validator.validate(client);

            if (expectedValid) {
                assertThat(violations).isEmpty();
            } else {
                assertThat(violations).isNotEmpty();
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage())
                        .contains(description);
            }
        }
    }
}