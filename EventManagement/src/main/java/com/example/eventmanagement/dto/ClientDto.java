package com.example.eventmanagement.dto;

import com.example.eventmanagement.entity.TicketReservation;

import java.util.List;

public record ClientDto(
        Long id,
        String fullName,
        String phoneNumber,
        String email
) { }

//    @JsonProperty("user_name") // Переименование поля в JSON
//     @JsonFormat(pattern = "yyyy-MM-dd") не знаю пока
