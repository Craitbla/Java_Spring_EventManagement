package com.example.eventmanagement.dto;

import jakarta.validation.constraints.Pattern;

public record PassportCreateDto(
        @Pattern(regexp = "\\d{4}", message = "Серия должна содержать 4 цифры")
        String series,
        @Pattern(regexp = "\\d{6}", message = "Номер должен содержать 6 цифр")
        String number
) {
}
