package com.example.eventmanagement.service.integration;

import com.example.eventmanagement.dto.EventCreateDto;
import com.example.eventmanagement.dto.EventDoneDto;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EventServiceIntegrationTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void createEvent_WithValidData_SavesToDatabase() {
        EventCreateDto createDto = new EventCreateDto(
                "Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), "Описание"
        );

        EventDoneDto result = eventService.createEvent(createDto);

        assertNotNull(result.id());
        assertEquals("Концерт", result.name());

        Event savedEvent = eventRepository.findById(result.id()).orElseThrow();
        assertEquals("Концерт", savedEvent.getName());
        assertEquals(100, savedEvent.getNumberOfSeats());
        assertEquals(EventStatus.PLANNED, savedEvent.getStatus());
    }

    @Test
    void updateEventStatus_ChangesStatusInDatabase() {
        EventCreateDto createDto = new EventCreateDto(
                "Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), "Описание"
        );

        EventDoneDto created = eventService.createEvent(createDto);

        eventService.updateEventStatus(created.id(), EventStatus.ONGOING);

        Event updatedEvent = eventRepository.findById(created.id()).orElseThrow();
        assertEquals(EventStatus.ONGOING, updatedEvent.getStatus());
    }

    @Test
    void getEventStatistics_ReturnsCorrectData() {
        EventCreateDto createDto = new EventCreateDto(
                "Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), "Описание"
        );

        EventDoneDto created = eventService.createEvent(createDto);

        var statistics = eventService.getEventStatistics(created.id());

        assertNotNull(statistics);
        assertEquals(created.id(), statistics.id());
        assertEquals("Концерт", statistics.name());
        assertEquals(0, statistics.confirmedTickets());
        assertEquals(BigDecimal.ZERO, statistics.totalRevenue());
    }
}