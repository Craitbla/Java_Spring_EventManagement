package com.example.eventmanagement.service.unit;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.exception.BusinessValidationException;
import com.example.eventmanagement.exception.EntityNotFoundException;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    void cleanupOldCanceledReservations_WhenOldCanceledReservationsExist_DeletesThem() {
        List<TicketReservation> oldCanceledReservations = Arrays.asList(
                TicketReservation.createForTesting(2, BookingStatus.CANCELED,
                        LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusMonths(2)),
                TicketReservation.createForTesting(1, BookingStatus.CANCELED,
                        LocalDateTime.now().minusMonths(3), LocalDateTime.now().minusMonths(3))
        );

        when(ticketReservationRepository.findByBookingStatusAndUpdatedAtBefore(
                eq(BookingStatus.CANCELED), any(LocalDateTime.class))
        ).thenReturn(oldCanceledReservations);

        int deletedCount = ticketReservationService.cleanupOldCanceledReservations();

        assertEquals(2, deletedCount);
        verify(ticketReservationRepository).deleteAll(oldCanceledReservations);
        verify(ticketReservationRepository).findByBookingStatusAndUpdatedAtBefore(
                eq(BookingStatus.CANCELED), any(LocalDateTime.class));
    }

    @Test
    void cleanupOldCanceledReservations_WhenNoOldCanceledReservations_ReturnsZero() {
        when(ticketReservationRepository.findByBookingStatusAndUpdatedAtBefore(
                eq(BookingStatus.CANCELED), any(LocalDateTime.class))
        ).thenReturn(Collections.emptyList());

        int deletedCount = ticketReservationService.cleanupOldCanceledReservations();

        assertEquals(0, deletedCount);
        verify(ticketReservationRepository, never()).deleteAll(any());
        verify(ticketReservationRepository).findByBookingStatusAndUpdatedAtBefore(
                eq(BookingStatus.CANCELED), any(LocalDateTime.class));
    }

    @Test
    void deleteCanceledReservation_Success() {
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CANCELED);
        reservation.setId(1L);

        when(ticketReservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ticketReservationService.deleteCanceledReservation(1L);

        verify(ticketReservationRepository).delete(reservation);
        verify(ticketReservationRepository).findById(1L);
    }

    @Test
    void deleteCanceledReservation_WhenReservationNotFound_ThrowsException() {
        when(ticketReservationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> ticketReservationService.deleteCanceledReservation(1L));

        verify(ticketReservationRepository, never()).delete(any());
    }

    @Test
    void deleteCanceledReservation_WhenReservationNotCanceled_ThrowsException() {
        TicketReservation reservation = new TicketReservation(2, BookingStatus.CONFIRMED);
        reservation.setId(1L);

        when(ticketReservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(BusinessValidationException.class,
                () -> ticketReservationService.deleteCanceledReservation(1L));

        verify(ticketReservationRepository, never()).delete(any());
    }
}