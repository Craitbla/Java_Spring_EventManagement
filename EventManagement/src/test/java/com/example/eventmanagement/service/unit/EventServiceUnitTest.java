package com.example.eventmanagement.service.unit;

import com.example.eventmanagement.dto.EventCreateDto;
import com.example.eventmanagement.dto.EventDoneDto;
import com.example.eventmanagement.dto.EventStatisticsDto;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.exception.BusinessValidationException;
import com.example.eventmanagement.exception.DuplicateEntityException;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.exception.OperationNotAllowedException;
import com.example.eventmanagement.mapper.EventMapper;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceUnitTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    @Test
    void createEvent_Success() {
        EventCreateDto createDto = new EventCreateDto(
                "Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание"
        );
        Event event = new Event("Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        EventDoneDto expectedDto = new EventDoneDto(1L, "Концерт", LocalDate.now().plusDays(10),
                100, BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание",
                LocalDateTime.now(), LocalDateTime.now());

        when(eventMapper.fromCreateWithoutDependenciesDto(createDto)).thenReturn(event);
        when(eventRepository.existsByNameAndDate("Концерт", LocalDate.now().plusDays(10))).thenReturn(false);
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toEventDoneDto(event)).thenReturn(expectedDto);

        EventDoneDto result = eventService.createEvent(createDto);

        assertNotNull(result);
        assertEquals("Концерт", result.name());
        verify(eventRepository).save(event);
    }

    @Test
    void createEvent_PastDate_ThrowsException() {
        EventCreateDto createDto = new EventCreateDto(
                "Концерт", LocalDate.now().minusDays(1), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание"
        );
        Event event = new Event("Концерт", LocalDate.now().minusDays(1), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");

        when(eventMapper.fromCreateWithoutDependenciesDto(createDto)).thenReturn(event);

        assertThrows(BusinessValidationException.class, () -> eventService.createEvent(createDto));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void createEvent_DuplicateEvent_ThrowsException() {
        EventCreateDto createDto = new EventCreateDto(
                "Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание"
        );
        Event event = new Event("Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");

        when(eventMapper.fromCreateWithoutDependenciesDto(createDto)).thenReturn(event);
        when(eventRepository.existsByNameAndDate("Концерт", LocalDate.now().plusDays(10))).thenReturn(true);

        assertThrows(DuplicateEntityException.class, () -> eventService.createEvent(createDto));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEventStatus_Success() {
        Event event = new Event("Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        event.setId(1L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);

        eventService.updateEventStatus(1L, EventStatus.ONGOING);

        assertEquals(EventStatus.ONGOING, event.getStatus());
        verify(eventRepository).save(event);
    }

    @Test
    void updateEventStatus_EventNotFound_ThrowsException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> eventService.updateEventStatus(1L, EventStatus.ONGOING));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEventStatus_InvalidTransition_ThrowsException() {
        Event event = new Event("Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.COMPLETED, "Описание");
        event.setId(1L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(BusinessValidationException.class, () -> eventService.updateEventStatus(1L, EventStatus.PLANNED));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void getEventStatistics_Success() {
        Event event = new Event("Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        event.setId(1L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.countConfirmedTicketsByEventId(1L)).thenReturn(50);

        EventStatisticsDto result = eventService.getEventStatistics(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(50, result.confirmedTickets());
        assertEquals(BigDecimal.valueOf(50000), result.totalRevenue());
    }

    @Test
    void getEventStatistics_EventNotFound_ThrowsException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> eventService.getEventStatistics(1L));
    }
}