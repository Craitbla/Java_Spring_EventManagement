package com.example.eventmanagement.converter;

import com.example.eventmanagement.enums.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingStatusConverterTest {

    private final BookingStatusConverter converter = new BookingStatusConverter();

    @Test
    void shouldConvertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(BookingStatus.PENDING_CONFIRMATION))
                .isEqualTo("ожидает подтверждения");
        assertThat(converter.convertToDatabaseColumn(BookingStatus.CONFIRMED))
                .isEqualTo("подтверждено");
        assertThat(converter.convertToDatabaseColumn(BookingStatus.CANCELED))
                .isEqualTo("отменено");
    }

    @ParameterizedTest
    @NullSource
    void shouldConvertNullToDatabaseColumn(BookingStatus status) {
        assertThat(converter.convertToDatabaseColumn(status)).isNull();
    }

    @ParameterizedTest
    @MethodSource("validStringToBookingStatusProvider")
    void shouldConvertToEntityAttribute(String dbData, BookingStatus expectedStatus) {
        assertThat(converter.convertToEntityAttribute(dbData)).isEqualTo(expectedStatus);
    }

    private static Stream<Arguments> validStringToBookingStatusProvider() {
        return Stream.of(
                Arguments.of("ожидает подтверждения", BookingStatus.PENDING_CONFIRMATION),
                Arguments.of("подтверждено", BookingStatus.CONFIRMED),
                Arguments.of("отменено", BookingStatus.CANCELED)
        );
    }

    @ParameterizedTest
    @NullSource
    void shouldConvertNullToEntityAttribute(String dbData) {
        assertThat(converter.convertToEntityAttribute(dbData)).isNull();
    }

    @Test
    void shouldThrowExceptionForInvalidDatabaseValue() {
        String invalidValue = "invalid_status";

        assertThatThrownBy(() -> converter.convertToEntityAttribute(invalidValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неизвестный статус: " + invalidValue);
    }

    @Test
    void shouldThrowExceptionForEmptyString() {
        String emptyValue = "";

        assertThatThrownBy(() -> converter.convertToEntityAttribute(emptyValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неизвестный статус: " + emptyValue);
    }

    @Test
    void shouldHandleCaseSensitivity() {
        String upperCaseValue = "CONFIRMED";

        assertThatThrownBy(() -> converter.convertToEntityAttribute(upperCaseValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неизвестный статус: " + upperCaseValue);
    }
}