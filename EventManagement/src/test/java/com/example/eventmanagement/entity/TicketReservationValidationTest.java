package com.example.eventmanagement.entity;

import com.example.eventmanagement.enums.BookingStatus;
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
class TicketReservationValidationTest {

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    static Stream<Arguments> ticketReservationDataProvider() {
        Client client = new Client("Test Client", "+79123456789", new Passport("1234", "567890"));
        Event event = new Event("Test Event", java.time.LocalDate.now().plusDays(10),
                java.math.BigDecimal.valueOf(100), com.example.eventmanagement.enums.EventStatus.PLANNED, "Description");

        return Stream.of(
                Arguments.of(createTicketReservation(client, event, 5, BookingStatus.PENDING_CONFIRMATION), true, "Валидно"),
                Arguments.of(createTicketReservation(client, event, 1, BookingStatus.CONFIRMED), true, "Валидно"),
                Arguments.of(createTicketReservation(client, event, 0, BookingStatus.PENDING_CONFIRMATION), false, "Количество билетов должно быть больше или равно 1"),
                Arguments.of(createTicketReservation(client, event, -1, BookingStatus.PENDING_CONFIRMATION), false, "Количество билетов должно быть больше или равно 1")
        );
    }

    private static TicketReservation createTicketReservation(Client client, Event event, Integer numberOfTickets, BookingStatus status) {
        TicketReservation reservation = new TicketReservation(numberOfTickets, status);
        reservation.assignClient(client);
        reservation.assignEvent(event);
        return reservation;
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @MethodSource("ticketReservationDataProvider")
    void validateTicketReservation(TicketReservation reservation, boolean expectedValid, String description) {
        Set<ConstraintViolation<TicketReservation>> violations = validator.validate(reservation);

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