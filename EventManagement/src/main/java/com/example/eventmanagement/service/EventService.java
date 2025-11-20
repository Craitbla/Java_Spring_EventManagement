package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.ClientDoneDto;
import com.example.eventmanagement.dto.EventCreateDto;
import com.example.eventmanagement.dto.EventDoneDto;
import com.example.eventmanagement.dto.EventStatisticsDto;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.exception.OperationNotAllowedException;
import com.example.eventmanagement.mapper.ClientMapper;
import com.example.eventmanagement.mapper.EventMapper;
import com.example.eventmanagement.repository.EventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.example.eventmanagement.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    EventService(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }
    //минимум, пока больше придумывать не буду

    // 1. Бизнес-логика статусов
    public void updateEventStatus(Long eventId, EventStatus newStatus) {
        Event foundedEvent = eventRepository.findById(eventId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Мероприятие по id %d не найдено", eventId))
        );
        EventStatus oldStatus = foundedEvent.getStatus();
        if (oldStatus == newStatus) {
            return;
        }
        if (!(oldStatus == EventStatus.PLANNED && newStatus == EventStatus.CANCELED ||
                oldStatus == EventStatus.PLANNED && newStatus == EventStatus.ONGOING ||
                oldStatus == EventStatus.CANCELED && newStatus == EventStatus.PLANNED ||
                oldStatus == EventStatus.ONGOING && newStatus == EventStatus.CANCELED ||
                oldStatus == EventStatus.ONGOING && newStatus == EventStatus.COMPLETED
        )) {
            throw new OperationNotAllowedException(String.format("Статус %s не может поменяться на %s", oldStatus, newStatus));
        }
        foundedEvent.setStatus(newStatus);
        eventRepository.save(foundedEvent);

//        // Проверить возможность смены статуса
//        // (например, нельзя отменить завершенное мероприятие)
//        // Обновить статус
    }
//
//    // 2. Статистика
    public EventStatisticsDto getEventStatistics(Long eventId) {
        Event foundedEvent = eventRepository.findById(eventId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Мероприятие по id %d не найдено", eventId))
        );
        Integer countedConfirmedTickets = eventRepository.countConfirmedTicketsByEventId(eventId);
        BigDecimal totalRevenue = foundedEvent.getTicketPrice().multiply(BigDecimal.valueOf(countedConfirmedTickets));
return new EventStatisticsDto(foundedEvent.getId(),
        foundedEvent.getName(), foundedEvent.getDate(), foundedEvent.getNumberOfSeats(), foundedEvent.getStatus(),
        countedConfirmedTickets, foundedEvent.getTicketPrice(), totalRevenue
        );
        //        // Получить базовую информацию о мероприятии
//        // Посчитать подтвержденные билеты
//        // Вычислить выручку
    }
//
//    // 3. Валидация дат
    public EventDoneDto createEvent(EventCreateDto eventDto) {
        Event event = eventMapper.fromCreateWithoutDependenciesDto(eventDto);
        if(eventDto.date().isBefore(LocalDate.now())){
            throw new OperationNotAllowedException("Нельзя создать мероприятие с прошедшей датой");
        }
        if(eventRepository.existsByNameAndDate(event.getName(), event.getDate())){
            throw new OperationNotAllowedException(String.format("Такое мероприятие уже существует: %s %s", event.getName(), event.getDate().toString()));
        }
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toEventDoneDto(savedEvent);
//        // Проверить что дата в будущем
//        // Проверить уникальность названия+даты
    }
}
