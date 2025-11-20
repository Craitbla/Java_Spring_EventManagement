package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.TicketReservationCreateDto;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.mapper.EventMapper;
import com.example.eventmanagement.mapper.TicketReservationMapper;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.PassportRepository;
import com.example.eventmanagement.repository.TicketReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TicketReservationService {
    private  final TicketReservationRepository ticketReservationRepository;
    private  final ClientRepository clientRepository;
    private  final EventRepository eventRepository;
    private final TicketReservationMapper ticketReservationMapper;
    public TicketReservationService(TicketReservationRepository ticketReservationRepository, ClientRepository clientRepository, EventRepository eventRepository, TicketReservationMapper ticketReservationMapper) {
        this.ticketReservationRepository = ticketReservationRepository;
        this.clientRepository = clientRepository;
        this.eventRepository = eventRepository;
        this.ticketReservationMapper = ticketReservationMapper;
    }

    public TicketReservation createReservation(TicketReservationCreateDto dto) {
        TicketReservation reservation = ticketReservationMapper.fromCreateWithoutDependenciesDto(dto);

        Client client = clientRepository.findById(dto.client().id())
                .orElseThrow(() -> new EntityNotFoundException("Клиент c id"+ dto.client().id() +" не найден"));
        Event event = eventRepository.findById(dto.event().id())
                .orElseThrow(() -> new EntityNotFoundException("Мероприятие c id"+ dto.event().id() +" не найден"));

        if(event.eventRepository.countConfirmedTicketsByEventId())
        client.addTicketReservation(reservation);
        event.addTicketReservation(reservation);

        return ticketReservationRepository.save(reservation);
    }
//// 1. Создание бронирования с проверками
//public TicketReservationDto createReservation(ReservationRequest request) {
//        // Проверить существование клиента и мероприятия
//        // Проверить доступность билетов
//        // Проверить бизнес-правила (дата мероприятия и т.д.)
//        // Создать бронирование
//        }
//
//// 2. Подтверждение/отмена брони
//public void confirmReservation(Long reservationId) {
//        // Проверить существование брони
//        // Проверить возможность подтверждения
//        // Обновить статус
//        }
//
//// 3. Бизнес-логика отмены
//public void cancelReservation(Long reservationId) {
//        // Проверить можно ли отменить (время до мероприятия)
//        // Обновить статус
//        }
}
