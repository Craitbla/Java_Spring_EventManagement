package com.example.eventmanagement.dto;

public record ClientCreateDto(
                         String fullName,
                         String phoneNumber,
                         String email
) { }