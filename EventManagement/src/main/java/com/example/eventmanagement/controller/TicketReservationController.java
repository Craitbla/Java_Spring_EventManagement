package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.enums.BookingStatus;
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
        log.info("Бронирование с id {} найдено. Клиент: {}, Мероприятие: {}",
                id,
                ticketReservation.client() != null ? ticketReservation.client().email() : "null",
                ticketReservation.event() != null ? ticketReservation.event().name() : "null");
        return ResponseEntity.ok(ticketReservation);
    }

    @PostMapping
    public ResponseEntity<TicketReservationDoneDto> createTicketReservation(@Valid @RequestBody TicketReservationCreateDto dto) {
        log.info("POST /api/ticketReservations - создание бронирования id клиента {} и id мероприятия {}", dto.clientId(), dto.eventId());
        TicketReservationDoneDto createdTicketReservation = ticketReservationService.createReservation(dto);
        log.info("Бронирование создано с id {}: id клиента {} и id мероприятия {}", createdTicketReservation.id(), dto.clientId(), dto.eventId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicketReservation);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<TicketReservationDoneDto> cancelTicketReservation(@PathVariable Long id) {
        log.info("PUT /api/ticketReservations/{}/cancel - отмена бронирования", id);
        TicketReservationDoneDto canceledTicketReservation = ticketReservationService.cancelReservation(id);
        log.info("Бронирование с id {} было отменено", id);
        return ResponseEntity.ok(canceledTicketReservation);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<TicketReservationDoneDto> confirmTicketReservation(@PathVariable Long id) {
        log.info("PUT /api/ticketReservations/{}/confirm - подтверждение бронирования", id);
        TicketReservationDoneDto confirmedTicketReservation = ticketReservationService.confirmReservation(id);
        log.info("Бронирование с id {} было подтверждено", id);
        return ResponseEntity.ok(confirmedTicketReservation);
    }

    @PostMapping("/cleanup/canceled-reservations")
    public ResponseEntity<CleanupResponse> cleanupOldCanceledReservations() {
        int deletedCount = ticketReservationService.cleanupOldCanceledReservations();

        if (deletedCount > 0) {
            return ResponseEntity.ok(new CleanupResponse(deletedCount, "Удалено " + deletedCount + " бронирований"));
        } else {
            return ResponseEntity.ok(new CleanupResponse(deletedCount,"Нет старых отмененных бронирований для очистки"));
        }
    }

}