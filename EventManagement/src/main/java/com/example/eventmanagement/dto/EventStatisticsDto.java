package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventStatisticsDto(
        Long id,
        String name,
        LocalDate date,
        EventStatus status,
        Long confirmedTickets,
        BigDecimal ticketPrice,
        BigDecimal totalRevenue

) {
}

