package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.EventStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventCreateDto(
        @NotBlank(message = "Название не может быть пустым")
        String name,
        @Future(message = "Дата должна быть будущей")
        LocalDate date,
        @Min(value = 1, message = "Количество мест должно быть больше или равно 1")
        Integer numberOfSeats,
        @DecimalMin(value = "0.0", inclusive = true, message = "Цена билета должна быть больше или равна 0")
        BigDecimal ticketPrice,
        EventStatus status,
        String description
) {
}
