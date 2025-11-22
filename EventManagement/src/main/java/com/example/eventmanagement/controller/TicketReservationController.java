package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.service.TicketReservationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/ticketReservations")
public class TicketReservationController {
    private final TicketReservationService ticketReservationService;

    public TicketReservationController(TicketReservationService ticketReservationService) {
        this.ticketReservationService = ticketReservationService;
    }

    @GetMapping
    public ResponseEntity<List<TicketReservationDto>> getAllTicketReservations() {
        log.info("GET /api/ticketReservations - получение списка всех бронирований");
        List<TicketReservationDto> ticketReservations = ticketReservationService.getAll();
        log.debug("Найдено бронирований: {}", ticketReservations.size());
        return ResponseEntity.ok(ticketReservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketReservationDoneDto> getTicketReservationById(@PathVariable Long id) {
        log.info("GET /api/ticketReservations/{} - получение бронирования по ID", id);
        TicketReservationDoneDto ticketReservation = ticketReservationService.getById(id);
        log.info("Бронирование с id {} найдено c почтой клиента {} и названием мероприятия {} и датой {} ", id, ticketReservation.client().email(), ticketReservation.event().name(), ticketReservation.event().date());
        return ResponseEntity.ok(ticketReservation);
    }

    @PostMapping
    public ResponseEntity<TicketReservationDoneDto> createTicketReservation(@Valid @RequestBody TicketReservationCreateDto dto) {
        log.info("POST /api/ticketReservations - создание бронирования id клиента {} и id мероприятия {}", dto.clientId(), dto.eventId());
        TicketReservationDoneDto createdTicketReservation = ticketReservationService.createReservation(dto);
        log.info("Бронирование создано с id {}: id клиента {} и id мероприятия {}", createdTicketReservation.id(), dto.clientId(), dto.eventId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicketReservation);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCanceledReservation(@PathVariable Long id) {
        log.info("Админское удаление отмененного бронирования ID: {}", id);
        ticketReservationService.deleteCanceledReservation(id);
        return ResponseEntity.noContent().build();
    }

//    @PutMapping("/{id}/status")
//    public ResponseEntity<TicketReservationDoneDto> updateTicketReservationStatus(@PathVariable Long id, @RequestBody TicketReservationStatus status) {
//        log.info("PUT /api/ticketReservations/{}/status - обновление бронирования на {}", id, status.getStr());
//        TicketReservationDoneDto updatedTicketReservation = ticketReservationService.updateTicketReservationStatus(id, status);
//        log.info("У бронирования с id {} обновлен статус на: {}", id, status.getStr());
//        return ResponseEntity.ok(updatedTicketReservation);
//    }

//    @DeleteMapping("/{id}") по идее не может быть
//    public ResponseEntity<Void> deleteTicketReservation(@PathVariable Long id) {
//        log.info("DELETE /api/ticketReservations/{} - удаление бронирования", id);
//        ticketReservationService.deleteReservation(id);
//        log.info("Бронирование с id {} удалено", id);
//        return ResponseEntity.noContent().build();
//    }
}