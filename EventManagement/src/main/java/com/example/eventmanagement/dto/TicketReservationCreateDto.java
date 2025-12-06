package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.BookingStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TicketReservationCreateDto(
        @NotNull(message = "Id клиента обязательно для заполнения")
        Long clientId,
        @NotNull(message = "Id мероприятия обязательно для заполнения")
        Long eventId,
        @NotNull(message = "Количество билетов обязательно для заполнения")
        @Min(value = 1, message = "Количество билетов должно быть больше или равно 1")
        Integer numberOfTickets,
        BookingStatus bookingStatus
) {
}
