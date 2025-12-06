package com.example.eventmanagement.converter;

import com.example.eventmanagement.enums.BookingStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BookingStatusConverter implements AttributeConverter<BookingStatus, String> {

    @Override
    public String convertToDatabaseColumn(BookingStatus bookingStatus) {
        return bookingStatus != null ? bookingStatus.getStr() : null;
    }

    @Override
    public BookingStatus convertToEntityAttribute(String s) {
        if (s == null) return null;
        return BookingStatus.fromString(s);
    }
}