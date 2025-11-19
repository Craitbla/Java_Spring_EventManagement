package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.BookingStatus;

import java.time.LocalDateTime;

public record TicketReservationDoneDto(
        Long id,
        ClientCreateDto client,
        EventCreateDto event,
        Integer numberOfTickets,
        BookingStatus bookingStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}