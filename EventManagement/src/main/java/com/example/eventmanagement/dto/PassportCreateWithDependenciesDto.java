package com.example.eventmanagement.dto;

import java.time.LocalDateTime;

public record PassportCreateWithDependenciesDto(
        String series,
        String number
) {
}
