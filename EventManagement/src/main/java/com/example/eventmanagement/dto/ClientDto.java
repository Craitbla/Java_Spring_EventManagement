package com.example.eventmanagement.dto;

import com.example.eventmanagement.entity.TicketReservation;

import java.util.List;

//с id и без временных меток, чтобы передавать базу дальше, без зависимостей, только клиент
//update
public record ClientDto( //в рекорде автоматические геттеры прописаны как бы
        Long id,
        String fullName,
        String phoneNumber,
        String email
) { }

//    @JsonProperty("user_name") // Переименование поля в JSON
//     @JsonFormat(pattern = "yyyy-MM-dd") не знаю пока
