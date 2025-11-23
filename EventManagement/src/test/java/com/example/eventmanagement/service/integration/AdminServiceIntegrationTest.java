package com.example.eventmanagement.service.integration;

import org.junit.jupiter.api.BeforeEach;

import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.repository.TicketReservationRepository;
import com.example.eventmanagement.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
class AdminServiceIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TicketReservationRepository ticketReservationRepository;

    @BeforeEach
    void setUp() {
        ticketReservationRepository.deleteAll();
    }

    @Test
    void cleanupOldCanceledReservations_ShouldDeleteOnlyOldCanceledReservations() {
        TicketReservation oldCanceled = TicketReservation.createForTesting(
                2, BookingStatus.CANCELED,
                LocalDateTime.now().minusMonths(2),
                LocalDateTime.now().minusMonths(2)
        );
        TicketReservation recentCanceled = TicketReservation.createForTesting(
                1, BookingStatus.CANCELED,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(10)
        );
        TicketReservation oldConfirmed = TicketReservation.createForTesting(
                3, BookingStatus.CONFIRMED,
                LocalDateTime.now().minusMonths(2),
                LocalDateTime.now().minusMonths(2)
        );

        ticketReservationRepository.save(oldCanceled);
        ticketReservationRepository.save(recentCanceled);
        ticketReservationRepository.save(oldConfirmed);

        int deletedCount = adminService.cleanupOldCanceledReservations();

        assertEquals(1, deletedCount);
        assertFalse(ticketReservationRepository.findById(oldCanceled.getId()).isPresent());
        assertTrue(ticketReservationRepository.findById(recentCanceled.getId()).isPresent());
        assertTrue(ticketReservationRepository.findById(oldConfirmed.getId()).isPresent());
    }

    @Test
    void cleanupOldCanceledReservations_WithMultipleOldCanceled_ShouldDeleteAll() {
        TicketReservation oldCanceled1 = TicketReservation.createForTesting(
                2, BookingStatus.CANCELED,
                LocalDateTime.now().minusMonths(3),
                LocalDateTime.now().minusMonths(3)
        );
        TicketReservation oldCanceled2 = TicketReservation.createForTesting(
                1, BookingStatus.CANCELED,
                LocalDateTime.now().minusMonths(2),
                LocalDateTime.now().minusMonths(2)
        );
        TicketReservation recentCanceled = TicketReservation.createForTesting(
                3, BookingStatus.CANCELED,
                LocalDateTime.now().minusWeeks(2),
                LocalDateTime.now().minusWeeks(2)
        );

        ticketReservationRepository.save(oldCanceled1);
        ticketReservationRepository.save(oldCanceled2);
        ticketReservationRepository.save(recentCanceled);

        int deletedCount = adminService.cleanupOldCanceledReservations();

        assertEquals(2, deletedCount);
        assertFalse(ticketReservationRepository.findById(oldCanceled1.getId()).isPresent());
        assertFalse(ticketReservationRepository.findById(oldCanceled2.getId()).isPresent());
        assertTrue(ticketReservationRepository.findById(recentCanceled.getId()).isPresent());
    }

    @Test
    void cleanupOldCanceledReservations_WithNoOldCanceled_ShouldDeleteNothing() {
        TicketReservation recentCanceled = TicketReservation.createForTesting(
                2, BookingStatus.CANCELED,
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(20)
        );
        TicketReservation oldConfirmed = TicketReservation.createForTesting(
                1, BookingStatus.CONFIRMED,
                LocalDateTime.now().minusMonths(3),
                LocalDateTime.now().minusMonths(3)
        );

        ticketReservationRepository.save(recentCanceled);
        ticketReservationRepository.save(oldConfirmed);

        int deletedCount = adminService.cleanupOldCanceledReservations();

        assertEquals(0, deletedCount);
        assertTrue(ticketReservationRepository.findById(recentCanceled.getId()).isPresent());
        assertTrue(ticketReservationRepository.findById(oldConfirmed.getId()).isPresent());
    }

    @Test
    void cleanupOldCanceledReservations_WithMixedStatuses_ShouldDeleteOnlyCanceled() {
        TicketReservation oldCanceled = TicketReservation.createForTesting(
                2, BookingStatus.CANCELED,
                LocalDateTime.now().minusMonths(2),
                LocalDateTime.now().minusMonths(2)
        );
        TicketReservation oldPending = TicketReservation.createForTesting(
                1, BookingStatus.PENDING_CONFIRMATION,
                LocalDateTime.now().minusMonths(2),
                LocalDateTime.now().minusMonths(2)
        );
        TicketReservation oldConfirmed = TicketReservation.createForTesting(
                3, BookingStatus.CONFIRMED,
                LocalDateTime.now().minusMonths(2),
                LocalDateTime.now().minusMonths(2)
        );

        ticketReservationRepository.save(oldCanceled);
        ticketReservationRepository.save(oldPending);
        ticketReservationRepository.save(oldConfirmed);

        int deletedCount = adminService.cleanupOldCanceledReservations();

        assertEquals(1, deletedCount);
        assertFalse(ticketReservationRepository.findById(oldCanceled.getId()).isPresent());
        assertTrue(ticketReservationRepository.findById(oldPending.getId()).isPresent());
        assertTrue(ticketReservationRepository.findById(oldConfirmed.getId()).isPresent());
    }
}