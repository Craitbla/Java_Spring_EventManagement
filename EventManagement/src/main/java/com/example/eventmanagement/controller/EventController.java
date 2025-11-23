package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.service.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> getAllEvents() {
        log.info("GET /api/events - получение списка всех мероприятий");
        List<EventDto> events = eventService.getAll();
        log.debug("Найдено мероприятий: {}", events.size());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDoneDto> getEventById(@PathVariable Long id) {
        log.info("GET /api/events/{} - получение мероприятия по ID", id);
        EventDoneDto event = eventService.getById(id);
        log.info("Мероприятие с id {} найдено: {}", id, event.name());
        return ResponseEntity.ok(event);
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<EventStatisticsDto> getEventStatisticsById(@PathVariable Long id) {
        log.info("GET /api/events/{} - получение статистики мероприятия по ID", id);
        EventStatisticsDto eventStatisticsDto = eventService.getEventStatistics(id);
        log.info("Статистика мероприятия с id {} найдена: забронированные билеты - {}, выручка - {}", id, eventStatisticsDto.confirmedTickets().toString(), eventStatisticsDto.totalRevenue().toString());
        return ResponseEntity.ok(eventStatisticsDto);
    }

    @PostMapping
    public ResponseEntity<EventDoneDto> createEvent(@Valid @RequestBody EventCreateDto dto) {
        log.info("POST /api/events - создание мероприятия: {} ({})", dto.name(), dto.date());
        EventDoneDto createdEvent = eventService.createEvent(dto);
        log.info("Мероприятие создано с id {}: {}", createdEvent.id(), createdEvent.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{id}/status") //по идее здесь тоже другое
    public ResponseEntity<EventDoneDto> updateEventStatus(@PathVariable Long id, @RequestBody EventStatus status) {
        log.info("PUT /api/events/{}/status - обновление мероприятия на {}", id, status.getStr());
        EventDoneDto updatedEvent = eventService.updateEventStatus(id, status);
        log.info("У мероприятия с id {} обновлен статус на: {}", id, status.getStr());
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        log.info("DELETE /api/events/{} - удаление мероприятия", id);
        eventService.deleteEvent(id);
        log.info("Мероприятие с id {} удалено", id);
        return ResponseEntity.noContent().build();
    }
}