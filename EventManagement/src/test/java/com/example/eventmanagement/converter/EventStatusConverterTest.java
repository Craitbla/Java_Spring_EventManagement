package com.example.eventmanagement.converter;

import com.example.eventmanagement.enums.EventStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventStatusConverterTest {

    private final EventStatusConverter converter = new EventStatusConverter();

    @Test
    void shouldConvertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(EventStatus.PLANNED))
                .isEqualTo("запланировано");
        assertThat(converter.convertToDatabaseColumn(EventStatus.ONGOING))
                .isEqualTo("проходит");
        assertThat(converter.convertToDatabaseColumn(EventStatus.COMPLETED))
                .isEqualTo("завершено");
        assertThat(converter.convertToDatabaseColumn(EventStatus.CANCELED))
                .isEqualTo("отменено");
    }

    @ParameterizedTest
    @NullSource
    void shouldConvertNullToDatabaseColumn(EventStatus status) {
        assertThat(converter.convertToDatabaseColumn(status)).isNull();
    }

    @ParameterizedTest
    @MethodSource("validStringToEventStatusProvider")
    void shouldConvertToEntityAttribute(String dbData, EventStatus expectedStatus) {
        assertThat(converter.convertToEntityAttribute(dbData)).isEqualTo(expectedStatus);
    }

    private static Stream<Arguments> validStringToEventStatusProvider() {
        return Stream.of(
                Arguments.of("запланировано", EventStatus.PLANNED),
                Arguments.of("проходит", EventStatus.ONGOING),
                Arguments.of("завершено", EventStatus.COMPLETED),
                Arguments.of("отменено", EventStatus.CANCELED)
        );
    }

    @ParameterizedTest
    @NullSource
    void shouldConvertNullToEntityAttribute(String dbData) {
        assertThat(converter.convertToEntityAttribute(dbData)).isNull();
    }

    @Test
    void shouldThrowExceptionForInvalidDatabaseValue() {
        String invalidValue = "invalid_event_status";

        assertThatThrownBy(() -> converter.convertToEntityAttribute(invalidValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный аргумент для конвертера строчки в статус мероприятия: " + invalidValue);
    }

    @Test
    void shouldThrowExceptionForEmptyString() {
        String emptyValue = "";

        assertThatThrownBy(() -> converter.convertToEntityAttribute(emptyValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный аргумент для конвертера строчки в статус мероприятия: " + emptyValue);
    }

    @Test
    void shouldHandleCaseSensitivity() {
        String upperCaseValue = "PLANNED";

        assertThatThrownBy(() -> converter.convertToEntityAttribute(upperCaseValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный аргумент для конвертера строчки в статус мероприятия: " + upperCaseValue);
    }

    @Test
    void shouldHandleWhitespaceStrings() {
        String whitespaceValue = "  запланировано  ";

        assertThatThrownBy(() -> converter.convertToEntityAttribute(whitespaceValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный аргумент для конвертера строчки в статус мероприятия: " + whitespaceValue);
    }
}