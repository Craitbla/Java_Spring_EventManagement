package com.example.eventmanagement.service.unit;

import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.repository.TicketReservationRepository;
import com.example.eventmanagement.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceUnitTest {

    @Mock
    private TicketReservationRepository ticketReservationRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void cleanupOldCanceledReservations_WhenReservationsExist_ShouldDeleteThem() {
        List<TicketReservation> mockReservations = Arrays.asList(
                new TicketReservation(),
                new TicketReservation()
        );

        when(ticketReservationRepository.findByBookingStatusAndUpdatedAtBefore(any(), any()))
                .thenReturn(mockReservations);

        int result = adminService.cleanupOldCanceledReservations();

        verify(ticketReservationRepository).deleteAll(mockReservations);
        assertEquals(2, result);
    }

    @Test
    void cleanupOldCanceledReservations_WhenNoReservations_ShouldReturnZero() {
        when(ticketReservationRepository.findByBookingStatusAndUpdatedAtBefore(any(), any()))
                .thenReturn(Collections.emptyList());

        int result = adminService.cleanupOldCanceledReservations();

        verify(ticketReservationRepository, never()).deleteAll(any());
        assertEquals(0, result);
    }

    @Test
    void cleanupOldCanceledReservations_ShouldUseCorrectParameters() {
        LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0);
        try (MockedStatic<LocalDateTime> localDateTimeMock = mockStatic(LocalDateTime.class)) {
            localDateTimeMock.when(LocalDateTime::now).thenReturn(fixedTime);

            LocalDateTime expectedMonthAgo = fixedTime.minusMonths(1);

            adminService.cleanupOldCanceledReservations();

            verify(ticketReservationRepository)
                    .findByBookingStatusAndUpdatedAtBefore(BookingStatus.CANCELED, expectedMonthAgo);
        }
    }
}