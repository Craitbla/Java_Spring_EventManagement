package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.exception.BusinessValidationException;
import com.example.eventmanagement.exception.DuplicateEntityException;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.mapper.EventMapper;
import com.example.eventmanagement.repository.EventRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.eventmanagement.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@Slf4j
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    EventService(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    public List<EventDto> getAll() {
        log.debug("Получение списка всех мероприятий");
        return eventMapper.toEventDtoList(eventRepository.findAll());
    }

    public EventDoneDto getById(Long id) {
        log.debug("Получение мероприятия по ID: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Мероприятие с id %d не найден", id)
                ));
        return eventMapper.toEventDoneDto(event);
    }

    public EventDoneDto updateEventStatus(Long eventId, EventStatus newStatus) {
        log.info("Обновление статуса мероприятия с ID: {} на {}", eventId, newStatus);
        Event foundedEvent = eventRepository.findById(eventId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Мероприятие по id %d не найдено", eventId))
        );
        EventStatus oldStatus = foundedEvent.getStatus();
        if (!(oldStatus == EventStatus.PLANNED && newStatus == EventStatus.CANCELED ||
                oldStatus == EventStatus.PLANNED && newStatus == EventStatus.ONGOING ||
                oldStatus == EventStatus.CANCELED && newStatus == EventStatus.PLANNED ||
                oldStatus == EventStatus.ONGOING && newStatus == EventStatus.CANCELED ||
                oldStatus == EventStatus.ONGOING && newStatus == EventStatus.COMPLETED
        ) || (oldStatus == newStatus)) {
            throw new BusinessValidationException(String.format("Статус %s не может поменяться на %s", oldStatus, newStatus));
        }
        foundedEvent.setStatus(newStatus);
        Event savedEvent = eventRepository.save(foundedEvent);
        log.info("Статус мероприятия с ID {} изменен с {} на {}", eventId, oldStatus, newStatus);
        return eventMapper.toEventDoneDto(savedEvent);
    }

    public EventStatisticsDto getEventStatistics(Long eventId) {
        log.debug("Получение статистики для мероприятия с ID: {}", eventId);
        Event foundedEvent = eventRepository.findById(eventId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Мероприятие по id %d не найдено", eventId))
        );
        Integer countedConfirmedTickets = eventRepository.countConfirmedTicketsByEventId(eventId).intValue();
        BigDecimal totalRevenue = foundedEvent.getTicketPrice().multiply(BigDecimal.valueOf(countedConfirmedTickets));
        return new EventStatisticsDto(foundedEvent.getId(),
                foundedEvent.getName(), foundedEvent.getDate(), foundedEvent.getNumberOfSeats(), foundedEvent.getStatus(),
                countedConfirmedTickets, foundedEvent.getTicketPrice(), totalRevenue
        );
    }

    public EventDoneDto createEvent(EventCreateDto eventDto) {
        log.info("Создание мероприятия: {}", eventDto.name());
        Event event = eventMapper.fromCreateWithoutDependenciesDto(eventDto);
        if (eventDto.date().isBefore(LocalDate.now())) {
            throw new BusinessValidationException("Нельзя создать мероприятие с прошедшей датой");
        }
        if (eventRepository.existsByNameAndDate(event.getName(), event.getDate())) {
            throw new DuplicateEntityException(String.format("Такое мероприятие уже существует: %s %s", event.getName(), event.getDate().toString()));
        }
        Event savedEvent = eventRepository.save(event);
        log.info("Мероприятие создано с ID: {}", savedEvent.getId());
        return eventMapper.toEventDoneDto(savedEvent);
    }

    public void deleteEvent(Long id) {
        log.info("Удаление мероприятия с ID: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Мероприятие с id %d не найдено", id)
                ));
        if (event.getStatus() == EventStatus.ONGOING || event.getStatus() == EventStatus.PLANNED) {
            throw new BusinessValidationException(String.format("Это мерроприятие с id %d нельзя удалить, оно еще проходит или будет проходить", id));
        }
        eventRepository.delete(event);
        log.info("Мероприятие с ID {} удалено", id);
    }
}