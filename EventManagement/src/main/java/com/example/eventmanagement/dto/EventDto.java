package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventDto ( //другие Dto потом
        Long id,
        String name,
        LocalDate date,
        BigDecimal ticketPrice,
        EventStatus status,
        String description
){ }

