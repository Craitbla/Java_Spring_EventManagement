package com.example.eventmanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

//ограничение пасспорта через изпользование PassportWithoutDependencies
public record ClientCreateWithDependenciesDto(
        String fullName,
        String phoneNumber,
        String email,
        PassportCreateDto passport

) {
}
