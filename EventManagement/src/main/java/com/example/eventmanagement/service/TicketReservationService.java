package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.exception.BusinessValidationException;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.mapper.TicketReservationMapper;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.TicketReservationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j
public class TicketReservationService {
    private final TicketReservationRepository ticketReservationRepository;
    private final ClientRepository clientRepository;
    private final EventRepository eventRepository;
    private final TicketReservationMapper ticketReservationMapper;

    public TicketReservationService(TicketReservationRepository ticketReservationRepository, ClientRepository clientRepository, EventRepository eventRepository, TicketReservationMapper ticketReservationMapper) {
        this.ticketReservationRepository = ticketReservationRepository;
        this.clientRepository = clientRepository;
        this.eventRepository = eventRepository;
        this.ticketReservationMapper = ticketReservationMapper;
    }

    public List<TicketReservationDto> getAll() {
        log.debug("Получение списка всех бронирований");
        return ticketReservationMapper.toTicketReservationDtoList(ticketReservationRepository.findAll());
    }

    public TicketReservationDoneDto getById(Long id) {
        log.debug("Получение бронирования по ID: {}", id);
        TicketReservation ticketReservation = ticketReservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Бронь с id %d не найдена", id)
                ));
        return ticketReservationMapper.toTicketReservationDoneDto(ticketReservation);
    }

    public TicketReservationDoneDto createReservation(TicketReservationCreateDto dto) {
        log.info("Создание бронирования для клиента {} на мероприятие {}", dto.clientId(), dto.eventId());
        TicketReservation reservation = ticketReservationMapper.fromCreateWithoutDependenciesDto(dto);

        Client client = clientRepository.findById(dto.clientId())
                .orElseThrow(() -> new EntityNotFoundException("Клиент c id" + dto.clientId() + " не найден"));
        Event event = eventRepository.findById(dto.eventId())
                .orElseThrow(() -> new EntityNotFoundException("Мероприятие c id" + dto.eventId() + " не найден"));
        Integer availableSeats = event.getNumberOfSeats() - eventRepository.countConfirmedOrPendingTicketsByEventId(event.getId()).intValue();
        if (availableSeats < dto.numberOfTickets()) {
            throw new BusinessValidationException(String.format("Билеты на мероприятие %s %s закончились", event.getName(), event.getDate().toString()));
        }
        if (!event.getStatus().isBookable()) {
            throw new BusinessValidationException(String.format("Бронирование билетов для мероприятия %s %s закрылось", event.getName(), event.getDate().toString()));
        }
        if (event.getDate().isBefore(LocalDate.now())) {
            throw new BusinessValidationException(String.format("Мероприятие %s %s уже прошло", event.getName(), event.getDate().toString()));
        }
        client.addTicketReservation(reservation);
        event.addTicketReservation(reservation);

        TicketReservation savedTicketReservation = ticketReservationRepository.save(reservation);
        log.info("Бронирование создано с ID: {}", savedTicketReservation.getId());
        return ticketReservationMapper.toTicketReservationDoneDto(savedTicketReservation);
    }

    public TicketReservationDoneDto confirmReservation(Long reservationId) {
        log.info("Подтверждение бронирования с ID: {}", reservationId);
        TicketReservation ticketReservation = ticketReservationRepository.findById(reservationId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Резервация по id %d не найдено", reservationId))
        );
        if (ticketReservation.getBookingStatus() == BookingStatus.CANCELED) {
            throw new BusinessValidationException(String.format("Подтверждение резервации по id %d невозможно после отмены бронирования", reservationId));
        }
        if (ticketReservation.getEvent().getDate().isBefore(LocalDate.now())) {
            throw new BusinessValidationException(String.format("Подтверждение резервации по id %d невозможно после того как мероприятие уже прошло", reservationId));
        }
        Integer confirmedTickets = eventRepository.countConfirmedTicketsByEventId(ticketReservation.getEvent().getId()).intValue();
        if (confirmedTickets + ticketReservation.getNumberOfTickets() > ticketReservation.getEvent().getNumberOfSeats()) {
            throw new BusinessValidationException(String.format("Недостаточно мест для подтверждения брони по id %d", reservationId));
        }
        ticketReservation.setBookingStatus(BookingStatus.CONFIRMED);
        TicketReservation canceledTicketReservation = ticketReservationRepository.save(ticketReservation);
        log.info("Бронирование с ID {} подтверждено", reservationId);
        return ticketReservationMapper.toTicketReservationDoneDto(canceledTicketReservation);
    }

    public TicketReservationDoneDto cancelReservation(Long reservationId) {
        log.info("Отмена бронирования с ID: {}", reservationId);
        TicketReservation ticketReservation = ticketReservationRepository.findById(reservationId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Резервация по id %d не найдена", reservationId))
        );
        if (ticketReservation.getEvent().getDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new BusinessValidationException(String.format("Отмена резервации по id %d невозможна позже чем за день до начала мероприятия", reservationId));
        }
        ticketReservation.setBookingStatus(BookingStatus.CANCELED);
        TicketReservation reservation = ticketReservationRepository.save(ticketReservation);
        log.info("Бронирование с ID {} отменено", reservationId);
        return ticketReservationMapper.toTicketReservationDoneDto(reservation);
    }
    public void deleteCanceledReservation(Long reservationId) {
        TicketReservation reservation = ticketReservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Резервация по id %d не найдена", reservationId)));

        if (reservation.getBookingStatus() != BookingStatus.CANCELED) {
            throw new BusinessValidationException(
                    "Можно удалять только отмененные бронирования. Текущий статус: " + reservation.getBookingStatus()
            );
        }

        ticketReservationRepository.delete(reservation);
        log.info("Отмененное бронирование {} удалено", reservationId);
    }

    public int cleanupOldCanceledReservations() {
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        List<TicketReservation> oldCanceled = ticketReservationRepository
                .findByBookingStatusAndUpdatedAtBefore(BookingStatus.CANCELED, monthAgo);

        if (!oldCanceled.isEmpty()) {
            log.info("Очистка {} старых отмененных бронирований", oldCanceled.size());
            ticketReservationRepository.deleteAll(oldCanceled);
        }
        return oldCanceled.size();
    }

}