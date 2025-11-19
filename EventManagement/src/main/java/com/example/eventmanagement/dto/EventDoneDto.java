package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventDoneDto(
        Long id,
        String name,
        LocalDate date,
        BigDecimal ticketPrice,
        EventStatus status,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
