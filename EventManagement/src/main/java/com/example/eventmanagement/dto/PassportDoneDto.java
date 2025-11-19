package com.example.eventmanagement.dto;

import java.time.LocalDateTime;

public record PassportDoneDto(
        Long id,
        String series,
        String number,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

