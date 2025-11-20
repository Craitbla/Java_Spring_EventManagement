package com.example.eventmanagement.service.unit;

import com.example.eventmanagement.dto.TicketReservationCreateDto;
import com.example.eventmanagement.dto.TicketReservationDoneDto;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.exception.OperationNotAllowedException;
import com.example.eventmanagement.mapper.TicketReservationMapper;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.TicketReservationRepository;
import com.example.eventmanagement.service.TicketReservationService;
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
class TicketReservationServiceUnitTest {

    @Mock
    private TicketReservationRepository ticketReservationRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketReservationMapper ticketReservationMapper;

    @InjectMocks
    private TicketReservationService ticketReservationService;

    @Test
    void createReservation_Success() {
        TicketReservationCreateDto createDto = new TicketReservationCreateDto(
                1L, 1L, 2, BookingStatus.PENDING_CONFIRMATION
        );
        Client client = new Client("Иван Иванов", "+79123456789", "ivan@mail.ru",
                new Passport("1234", "567890"));
        client.setId(1L);
        Event event = new Event("Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        event.setId(1L);
        TicketReservation reservation = new TicketReservation(2, BookingStatus.PENDING_CONFIRMATION);
        TicketReservationDoneDto expectedDto = new TicketReservationDoneDto(
                1L, null, null, 2, BookingStatus.PENDING_CONFIRMATION,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(ticketReservationMapper.fromCreateWithoutDependenciesDto(createDto)).thenReturn(reservation);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.countConfirmedTicketsByEventId(1L)).thenReturn(50);
        when(ticketReservationRepository.save(reservation)).thenReturn(reservation);
        when(ticketReservationMapper.toTicketReservationDoneDto(reservation)).thenReturn(expectedDto);

        TicketReservationDoneDto result = ticketReservationService.createReservation(createDto);

        assertNotNull(result);
        assertEquals(2, result.numberOfTickets());
        verify(ticketReservationRepository).save(reservation);
    }

    @Test
    void createReservation_ClientNotFound_ThrowsException() {
        TicketReservationCreateDto createDto = new TicketReservationCreateDto(
                1L, 1L, 2, BookingStatus.PENDING_CONFIRMATION
        );
        TicketReservation reservation = new TicketReservation(2, BookingStatus.PENDING_CONFIRMATION);

        when(ticketReservationMapper.fromCreateWithoutDependenciesDto(createDto)).thenReturn(reservation);
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> ticketReservationService.createReservation(createDto));
        verify(ticketReservationRepository, never()).save(any());
    }

    @Test
    void createReservation_EventNotFound_ThrowsException() {
        TicketReservationCreateDto createDto = new TicketReservationCreateDto(
                1L, 1L, 2, BookingStatus.PENDING_CONFIRMATION
        );
        Client client = new Client("Иван Иванов", "+79123456789", "ivan@mail.ru",
                new Passport("1234", "567890"));
        TicketReservation reservation = new TicketReservation(2, BookingStatus.PENDING_CONFIRMATION);

        when(ticketReservationMapper.fromCreateWithoutDependenciesDto(createDto)).thenReturn(reservation);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> ticketReservationService.createReservation(createDto));
        verify(ticketReservationRepository, never()).save(any());
    }

    @Test
    void createReservation_NotEnoughSeats_ThrowsException() {
        TicketReservationCreateDto createDto = new TicketReservationCreateDto(
                1L, 1L, 60, BookingStatus.PENDING_CONFIRMATION
        );
        Client client = new Client("Иван Иванов", "+79123456789", "ivan@mail.ru",
                new Passport("1234", "567890"));
        client.setId(1L);
        Event event = new Event("Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        event.setId(1L);
        TicketReservation reservation = new TicketReservation(60, BookingStatus.PENDING_CONFIRMATION);

        when(ticketReservationMapper.fromCreateWithoutDependenciesDto(createDto)).thenReturn(reservation);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.countConfirmedTicketsByEventId(1L)).thenReturn(50);

        assertThrows(OperationNotAllowedException.class, () -> ticketReservationService.createReservation(createDto));
        verify(ticketReservationRepository, never()).save(any());
    }

    @Test
    void createReservation_EventCanceled_ThrowsException() {
        TicketReservationCreateDto createDto = new TicketReservationCreateDto(
                1L, 1L, 2, BookingStatus.PENDING_CONFIRMATION
        );
        Client client = new Client("Иван Иванов", "+79123456789", "ivan@mail.ru",
                new Passport("1234", "567890"));
        client.setId(1L);
        Event event = new Event("Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.CANCELED, "Описание");
       event.setId(1L);
        TicketReservation reservation = new TicketReservation(2, BookingStatus.PENDING_CONFIRMATION);

        when(ticketReservationMapper.fromCreateWithoutDependenciesDto(createDto)).thenReturn(reservation);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.countConfirmedTicketsByEventId(1L)).thenReturn(50);

        assertThrows(OperationNotAllowedException.class, () -> ticketReservationService.createReservation(createDto));
        verify(ticketReservationRepository, never()).save(any());
    }

    @Test
    void confirmReservation_Success() {
        Event event = new Event("Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        TicketReservation reservation = new TicketReservation(2, BookingStatus.PENDING_CONFIRMATION);
        event.addTicketReservation(reservation);
//        reservation.setEvent(event);

        when(ticketReservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(eventRepository.countConfirmedTicketsByEventId(any())).thenReturn(50);
        when(ticketReservationRepository.save(reservation)).thenReturn(reservation);

        ticketReservationService.confirmReservation(1L);

        assertEquals(BookingStatus.CONFIRMED, reservation.getBookingStatus());
        verify(ticketReservationRepository).save(reservation);
    }

    @Test
    void confirmReservation_AlreadyCanceled_ThrowsException() {
        Event event = new Event("Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CANCELED);
//        reservation.setEvent(event);
        event.addTicketReservation(reservation);
        when(ticketReservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(OperationNotAllowedException.class, () -> ticketReservationService.confirmReservation(1L));
        verify(ticketReservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_Success() {
        Event event = new Event("Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        TicketReservation reservation = new TicketReservation(2, BookingStatus.PENDING_CONFIRMATION);
//        reservation.setEvent(event);
        event.addTicketReservation(reservation);
        when(ticketReservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(ticketReservationRepository.save(reservation)).thenReturn(reservation);

        ticketReservationService.cancelReservation(1L);

        assertEquals(BookingStatus.CANCELED, reservation.getBookingStatus());
        verify(ticketReservationRepository).save(reservation);
    }

    @Test
    void cancelReservation_TooLate_ThrowsException() {
        Event event = new Event("Концерт", LocalDate.now().minusDays(2), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание");
        TicketReservation reservation = new TicketReservation(2, BookingStatus.PENDING_CONFIRMATION);
//        reservation.setEvent(event);
        event.addTicketReservation(reservation);
        when(ticketReservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(OperationNotAllowedException.class, () -> ticketReservationService.cancelReservation(1L));
        verify(ticketReservationRepository, never()).save(any());
    }
}