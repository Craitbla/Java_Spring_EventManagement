package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.BookingStatus;

public record TicketReservationCreateDto(
        ClientDoneDto client,
        EventDoneDto event,
        Integer numberOfTickets,
        BookingStatus bookingStatus
) {
}
