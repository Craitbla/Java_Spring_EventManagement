package com.example.eventmanagement.dto;

//без id и временных меток, чтобы создавать
//create
public record ClientCreateDto(
                         String fullName,
                         String phoneNumber,
                         String email
) { }