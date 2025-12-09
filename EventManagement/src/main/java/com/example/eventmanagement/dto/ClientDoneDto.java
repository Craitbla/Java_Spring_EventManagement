package com.example.eventmanagement.dto;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ClientDoneDto(
        Long id,
        String fullName,
        String phoneNumber,
        String email,
        PassportCreateDto passport,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt

) {
}