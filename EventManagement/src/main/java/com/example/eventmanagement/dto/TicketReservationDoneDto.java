package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record TicketReservationDoneDto(
        Long id,
        ClientCreateDto client,
        EventCreateDto event,
        Integer numberOfTickets,
        BookingStatus bookingStatus,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {
}