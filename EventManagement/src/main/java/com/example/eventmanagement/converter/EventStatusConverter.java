package com.example.eventmanagement.converter;

import com.example.eventmanagement.enums.EventStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter(autoApply = true)
public class EventStatusConverter implements AttributeConverter<EventStatus, String> {

    @Override
    public String convertToDatabaseColumn(EventStatus eventStatus) {
        return eventStatus != null ? eventStatus.getStr() : null;
    }

    @Override
    public EventStatus convertToEntityAttribute(String s) {
        if (s == null) return null;
        return EventStatus.fromString(s);
    }
}
