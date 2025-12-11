package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventStatisticsDto(
        Long id,
        String name,
        LocalDate date,
        Integer numberOfSeats,
        EventStatus status,
        Integer confirmedTickets,
        BigDecimal ticketPrice,
        BigDecimal totalRevenue

) {
}

