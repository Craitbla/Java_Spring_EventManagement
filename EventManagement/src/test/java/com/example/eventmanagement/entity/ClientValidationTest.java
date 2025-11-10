package com.example.eventmanagement.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class ClientValidationTest {
    private static final Passport defaultPassport1 = new Passport("1234", "567890");

    private static final String validFullName = "Иванова Карина Олеговна";
    private static final String validPhone = "+79123456789";
    private static final String validEmail = "valid@email.com";

    Client createDefaultClient1() {
        return new Client(validFullName, validPhone, validEmail, defaultPassport1);
    }

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    static Stream<Arguments> clientDataProvider() {
        return Stream.of(
                // valid data
                Arguments.of(new Client(validFullName, validPhone, validEmail, defaultPassport1), true, "Валидно"),
                Arguments.of(new Client("Карина", "+79001112233", "alice@test.ru", defaultPassport1), true, "Валидно"),
//
                // invalid data
                Arguments.of(new Client(null, validPhone, validEmail, defaultPassport1), false, "ФИО не может быть пустым"),
                Arguments.of(new Client("", validPhone, validEmail, defaultPassport1), false, "Имя должно иметь длину между 2 и 100 буквами"),
                Arguments.of(new Client("И", validPhone, validEmail, defaultPassport1), false, "Имя должно иметь длину между 2 и 100 буквами"),
                Arguments.of(new Client("И".repeat(101), validPhone, validEmail, defaultPassport1), false, "Имя должно иметь длину между 2 и 100 буквами"),

                Arguments.of(new Client(validFullName, "79123456789", validEmail, defaultPassport1), false, "Телефон должен начинаться с +7 и иметь 11 цифр"),
                Arguments.of(new Client(validFullName, "", validEmail, defaultPassport1), false, "Телефон обязателен"),
                Arguments.of(new Client(validFullName, null, validEmail, defaultPassport1), false, "Телефон обязателен"),
                Arguments.of(new Client(validFullName, validPhone, "invalid-email", defaultPassport1), false, "Email должен быть валидным")

        );
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @MethodSource("clientDataProvider")
    void validateClient(Client client, boolean expectedValid, String description) {
        Set<ConstraintViolation<Client>> violations = validator.validate(client);

        if (expectedValid) {
            assertThat(violations).isEmpty();
        } else if (violations.size() == 1) {
            assertThat(violations).isNotEmpty();

            assertThat(violations.iterator().next().getMessage())
                    .contains(description);
        } else if (violations.size() == 2) {
            boolean hasExpected = violations.stream().anyMatch(v -> v.getMessage().contains(description));
            assertThat(hasExpected).as("Expected violation with message containing: %s, but found violations: %s",
                            description, violations.stream().map(ConstraintViolation::getMessage).toList())
                    .isTrue();
        }
    }

}