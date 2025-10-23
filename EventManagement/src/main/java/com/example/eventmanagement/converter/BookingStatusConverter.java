package com.example.eventmanagement.converter;

import com.example.eventmanagement.enums.BookingStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;


@Converter(autoApply = true)
public class BookingStatusConverter implements AttributeConverter<BookingStatus, String> {

    @Override
    public String convertToDatabaseColumn(BookingStatus bookingStatus) {
        return bookingStatus!=null? bookingStatus.getStr():null;
    }

    @Override
    public BookingStatus convertToEntityAttribute(String s) {
        if(s==null) return null;
        return Arrays.stream(BookingStatus.values()).filter(status -> status.getStr().equals(s)).findFirst().orElseThrow(()-> new IllegalArgumentException("Неверный аргумент для конвертера строчки в статус бронирования: " + s));
    }
}
