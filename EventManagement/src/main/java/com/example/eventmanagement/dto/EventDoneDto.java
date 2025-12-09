package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventDoneDto(
        Long id,
        String name,
        LocalDate date,
        Integer numberOfSeats,
        BigDecimal ticketPrice,
        EventStatus status,
        String description,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt

) {
}
