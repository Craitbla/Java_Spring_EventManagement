package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.BookingStatus;

public record TicketReservationDto(
        Long id,
        ClientCreateDto client,
        EventCreateDto event,
        Integer numberOfTickets,
        BookingStatus bookingStatus

) {
}
