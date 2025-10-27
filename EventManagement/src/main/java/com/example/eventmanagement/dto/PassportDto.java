package com.example.eventmanagement.dto;

import com.example.eventmanagement.entity.Client;

import java.time.LocalDateTime;

public record PassportDto(
        Long id,
        String series,
        String number
) {
}
