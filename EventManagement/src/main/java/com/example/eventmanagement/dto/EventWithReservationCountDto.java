package com.example.eventmanagement.dto;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
public record EventWithReservationCountDto(
        Event event,
        Long reservationCount
) {
}
