package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.TicketReservationCreateDto;
import com.example.eventmanagement.dto.TicketReservationDoneDto;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.exception.OperationNotAllowedException;
import com.example.eventmanagement.mapper.EventMapper;
import com.example.eventmanagement.mapper.TicketReservationMapper;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.PassportRepository;
import com.example.eventmanagement.repository.TicketReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
@Transactional
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

    public TicketReservationDoneDto createReservation(TicketReservationCreateDto dto) {
        TicketReservation reservation = ticketReservationMapper.fromCreateWithoutDependenciesDto(dto);

        Client client = clientRepository.findById(dto.client().id())
                .orElseThrow(() -> new EntityNotFoundException("Клиент c id" + dto.client().id() + " не найден"));
        Event event = eventRepository.findById(dto.event().id())
                .orElseThrow(() -> new EntityNotFoundException("Мероприятие c id" + dto.event().id() + " не найден"));

        if ((event.getNumberOfSeats() - eventRepository.countConfirmedTicketsByEventId(event.getId())) > 0) {
            throw new OperationNotAllowedException(String.format("Билеты на мероприятие %s %s закончились", event.getName(), event.getDate().toString()));
        }
        if (event.getStatus() == EventStatus.CANCELED || event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.ONGOING) {
            throw new OperationNotAllowedException(String.format("Бронирование билетов для мероприятия %s %s закрылось", event.getName(), event.getDate().toString()));
        }
        if (event.getDate().isBefore(LocalDate.now())) {     //на всякий
            throw new OperationNotAllowedException(String.format("Мероприятие %s %s уже прошло", event.getName(), event.getDate().toString()));
        }
        client.addTicketReservation(reservation);
        event.addTicketReservation(reservation);

        TicketReservation savedTicketReservation = ticketReservationRepository.save(reservation);
        return ticketReservationMapper.toTicketReservationDoneDto(savedTicketReservation);
        // Проверить существование клиента и мероприятия
//        // Проверить доступность билетов
//        // Проверить бизнес-правила (дата мероприятия и т.д.)
//        // Создать бронирование
    }

    //// 2. Подтверждение/отмена брони
    public void confirmReservation(Long reservationId) {
        TicketReservation ticketReservation = ticketReservationRepository.findById(reservationId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Резервация по id %d не найдено", reservationId))
        );
        if (ticketReservation.getBookingStatus() == BookingStatus.CANCELED) {
            throw new OperationNotAllowedException(String.format("Подтверждение резервации по id %d невозможно после отмены бронирования", reservationId));
        }
        if (ticketReservation.getEvent().getDate().isBefore(LocalDate.now())) {
            throw new OperationNotAllowedException(String.format("Подтверждение резервации по id %d невозможно после того как мероприятие уже прошло", reservationId));
        }
        ticketReservation.setBookingStatus(BookingStatus.CONFIRMED);
        ticketReservationRepository.save(ticketReservation);
//        // Проверить существование брони
//        // Проверить возможность подтверждения
//        // Обновить статус
    }

    //
//// 3. Бизнес-логика отмены
    public void cancelReservation(Long reservationId) {
        TicketReservation ticketReservation = ticketReservationRepository.findById(reservationId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Резервация по id %d не найдено", reservationId))
        );
        if (ticketReservation.getEvent().getDate().isBefore(LocalDate.now().minusDays(1))) {
            throw new OperationNotAllowedException(String.format("Отмена резервации по id %d невозможна позже чем за день до начала мероприятия", reservationId));
        }
        ticketReservation.setBookingStatus(BookingStatus.CANCELED);
        ticketReservationRepository.save(ticketReservation);
//        // Проверить можно ли отменить (время до мероприятия)
//        // Обновить статус
    }
}
