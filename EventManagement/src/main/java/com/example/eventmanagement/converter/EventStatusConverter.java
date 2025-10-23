package com.example.eventmanagement.converter;

import com.example.eventmanagement.enums.EventStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter(autoApply = true) //Конвертер применится автоматически
public class EventStatusConverter implements AttributeConverter<EventStatus, String> {
    @Override
    public String convertToDatabaseColumn(EventStatus eventStatus) {
        return eventStatus != null ? eventStatus.getStr() : null;
    }

    @Override
    public EventStatus convertToEntityAttribute(String s) {
        if (s == null) return null;
        return Arrays.stream(EventStatus.values()).filter(status -> status.getStr().equals(s)).findFirst().orElseThrow(() -> new IllegalArgumentException("Неверный аргумент для конвертера строчки в статус мероприятия: " + s));
    }
}

