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
class PassportValidationTest {

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    static Stream<Arguments> passportDataProvider() {
        return Stream.of(
                Arguments.of(new Passport("1234", "567890"), true, "Валидно"),
                Arguments.of(new Passport("9999", "000000"), true, "Валидно"),
                Arguments.of(new Passport("123", "567890"), false, "Серия должна содержать 4 цифры"),
                Arguments.of(new Passport("12345", "567890"), false, "Серия должна содержать 4 цифры"),
                Arguments.of(new Passport("12a4", "567890"), false, "Серия должна содержать 4 цифры"),
                Arguments.of(new Passport("1234", "12345"), false, "Нормер должен содержать 6 цифр"),
                Arguments.of(new Passport("1234", "1234567"), false, "Нормер должен содержать 6 цифр"),
                Arguments.of(new Passport("1234", "12345a"), false, "Нормер должен содержать 6 цифр")
        );
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @MethodSource("passportDataProvider")
    void validatePassport(Passport passport, boolean expectedValid, String description) {
        Set<ConstraintViolation<Passport>> violations = validator.validate(passport);

        if (expectedValid) {
            assertThat(violations).isEmpty();
        } else if (violations.size() == 1) {
            assertThat(violations).isNotEmpty();
            assertThat(violations.iterator().next().getMessage()).contains(description);
        } else {
            boolean hasExpected = violations.stream().anyMatch(v -> v.getMessage().contains(description));
            assertThat(hasExpected).as("Expected violation with message containing: %s, but found violations: %s",
                            description, violations.stream().map(ConstraintViolation::getMessage).toList())
                    .isTrue();
        }
    }
}