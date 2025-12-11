package com.example.eventmanagement.dto;

import com.example.eventmanagement.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventCreateDto(
        @NotNull(message = "Название обязательно для заполнения")
        @NotBlank(message = "Название не может быть пустым")
        String name,
        @NotNull(message = "Дата обязательна для заполнения")
        @Future(message = "Дата должна быть будущей")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull(message = "Количество мест обязательно для заполнения")
        @Min(value = 1, message = "Количество мест должно быть больше или равно 1")
        Integer numberOfSeats,
        @NotNull(message = "Цена билета обязательна для заполнения")
        @DecimalMin(value = "0.0", inclusive = true, message = "Цена билета должна быть больше или равна 0")
        BigDecimal ticketPrice,
        String description
) {
}
