package com.example.eventmanagement.dto;

//самое главное, не нулл по бизнесс логике
//вот для чего это сложный вопрос
public record ClientSummaryDto(
        Long id,
        String fullName,
        String phoneNumber
) {
}
