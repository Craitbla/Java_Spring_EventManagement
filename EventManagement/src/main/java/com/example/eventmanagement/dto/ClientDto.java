package com.example.eventmanagement.dto;

import com.example.eventmanagement.entity.TicketReservation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ClientDto(
                         Long id,
                         @NotBlank(message = "ФИО не может быть пустым")
                         @Size(min = 2, max = 100, message = "Имя должно иметь длину между 2 и 100 буквами")
                         String fullName,
                         @NotBlank(message = "Телефон обязателен")
                         @Pattern(regexp = "^\\+7\\d{10}$", message = "Телефон должен начинаться с +7 и иметь 11 цифр")
                         String phoneNumber,
                         @Email(message = "Email должен быть валидным")
                         String email
) {
}

