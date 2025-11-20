package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.BookingStatus;

public record TicketReservationCreateDto(
        Long clientId,
        Long eventId,
        Integer numberOfTickets,
        BookingStatus bookingStatus
) {
}
