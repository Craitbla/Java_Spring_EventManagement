package com.example.eventmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

//ограничение пасспорта через изпользование PassportWithoutDependencies
public record ClientCreateWithDependenciesDto(
        @NotBlank(message = "ФИО не может быть пустым")
        @Size(min = 2, max = 100, message = "Имя должно иметь длину между 2 и 100 буквами")
        String fullName,
        @NotBlank(message = "Телефон обязателен")
        @Pattern(regexp = "^\\+7\\d{10}$", message = "Телефон должен начинаться с +7 и иметь 11 цифр")
        String phoneNumber,
        @Email(message = "Email должен быть валидным") //есть два варианта из разных библиотек
        String email,
        @Valid
        @NotNull(message = "Паспорт обязателен") //возможно что-то сломает
        PassportCreateDto passport

) {
}
