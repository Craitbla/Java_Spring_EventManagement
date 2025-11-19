package com.example.eventmanagement.dto;


import java.time.LocalDateTime;

public record ClientDoneDto(
        Long id,
        String fullName,
        String phoneNumber,
        String email,
        PassportCreateDto passport,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}