package com.example.eventmanagement.entity;

import com.example.eventmanagement.enums.EventStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class EventValidationTest {
    private static final String validName = "Test Event";
    private static final LocalDate validDate = LocalDate.now().plusDays(1);
    private static final BigDecimal validPrice = BigDecimal.valueOf(100);

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    static Stream<Arguments> eventDataProvider() {
        return Stream.of(
                Arguments.of(new Event(validName, validDate, validPrice, EventStatus.PLANNED, "Description"), true, "Валидно"),
                Arguments.of(new Event("Concert", LocalDate.now().plusDays(10), BigDecimal.valueOf(50), EventStatus.CANCELLED, null), true, "Валидно"),
                Arguments.of(new Event(null, validDate, validPrice, EventStatus.PLANNED, "Desc"), false, "Название не может быть пустым"),
                Arguments.of(new Event("", validDate, validPrice, EventStatus.PLANNED, "Desc"), false, "Название не может быть пустым"),
                Arguments.of(new Event("   ", validDate, validPrice, EventStatus.PLANNED, "Desc"), false, "Название не может быть пустым"),
                Arguments.of(new Event(validName, LocalDate.now().minusDays(1), validPrice, EventStatus.PLANNED, "Desc"), false, "Дата должна быть будущей"),
                Arguments.of(new Event(validName, validDate, BigDecimal.valueOf(-1), EventStatus.PLANNED, "Desc"), false, "Цена билета должна быть больше или равна 0")
        );
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @MethodSource("eventDataProvider")
    void validateEvent(Event event, boolean expectedValid, String description) {
        Set<ConstraintViolation<Event>> violations = validator.validate(event);

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