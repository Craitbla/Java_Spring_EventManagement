package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.BookingStatus;
import jakarta.validation.constraints.Min;

public record TicketReservationCreateDto(
        Long clientId,
        Long eventId,
        @Min(value = 1, message = "Количество билетов должно быть больше или равно 1")
        Integer numberOfTickets
) {
}
