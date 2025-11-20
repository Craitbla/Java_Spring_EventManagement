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
    private static final Integer validNumberOfSeats = 100;

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    static Stream<Arguments> eventDataProvider() {
        return Stream.of(
                Arguments.of(new Event(validName, validDate,validNumberOfSeats, validPrice, EventStatus.PLANNED, "Desc"), true, "Валидно"),
                Arguments.of(new Event("Concert", LocalDate.now().plusDays(10),validNumberOfSeats, BigDecimal.valueOf(50), EventStatus.CANCELED, null), true, "Валидно"),
                Arguments.of(new Event(null, validDate,validNumberOfSeats, validPrice, EventStatus.PLANNED, "Desc"), false, "Название не может быть пустым"),
                Arguments.of(new Event("", validDate,validNumberOfSeats, validPrice, EventStatus.PLANNED, "Desc"), false, "Название не может быть пустым"),
                Arguments.of(new Event("   ", validDate,validNumberOfSeats, validPrice, EventStatus.PLANNED, "Desc"), false, "Название не может быть пустым"),
                Arguments.of(new Event(validName, LocalDate.now().minusDays(1),validNumberOfSeats, validPrice, EventStatus.PLANNED, "Desc"), false, "Дата должна быть будущей"),
                Arguments.of(new Event(validName, validDate,validNumberOfSeats, BigDecimal.valueOf(-1), EventStatus.PLANNED, "Desc"), false, "Цена билета должна быть больше или равна 0"),
                Arguments.of(new Event(validName, validDate,1, validPrice, EventStatus.PLANNED, "Desc"), true,"Валидно"),
                Arguments.of(new Event(validName, validDate, 0, validPrice, EventStatus.PLANNED, "Desc"), false, "Количество мест должно быть больше или равно 1"),
                Arguments.of(new Event(validName, validDate, -100, validPrice, EventStatus.PLANNED, "Desc"), false, "Количество мест должно быть больше или равно 1")
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