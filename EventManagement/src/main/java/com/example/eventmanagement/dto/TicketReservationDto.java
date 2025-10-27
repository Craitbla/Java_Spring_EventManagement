package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.BookingStatus;

public record TicketReservationDto(
        Long id,
        ClientSummaryDto client,
        EventSummaryDto event,
        Integer numberOfTickets,
        BookingStatus bookingStatus

) {
}
