package com.example.eventmanagement.service.integration;

import com.example.eventmanagement.entity.*;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.repository.*;
import com.example.eventmanagement.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminServiceIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TicketReservationRepository ticketReservationRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PassportRepository passportRepository;

    @Autowired
    private EntityManager entityManager;

    private Client testClient;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        ticketReservationRepository.deleteAll();
        clientRepository.deleteAll();
        eventRepository.deleteAll();
        passportRepository.deleteAll();

        Passport passport = new Passport("1234", "567890");
        passport = passportRepository.save(passport);

        testClient = new Client("Test Client", "+79123456789", "test@mail.ru", passport);
        testClient = clientRepository.save(testClient);

        testEvent = new Event("Test Event", LocalDate.now().plusDays(10), 100, BigDecimal.valueOf(500), EventStatus.PLANNED, "Description");
        testEvent = eventRepository.save(testEvent);
    }

    private TicketReservation createReservationWithClientAndEvent(BookingStatus status, LocalDateTime updatedAt) {
        TicketReservation reservation = new TicketReservation(2, status);
        testClient.addTicketReservation(reservation);
        testEvent.addTicketReservation(reservation);
        reservation.setUpdatedAt(updatedAt);
        return ticketReservationRepository.save(reservation);
    }

    @Test
    void cleanupOldCanceledReservations_ShouldDeleteOnlyOldCanceledReservations() {
        TicketReservation oldCanceled = createReservationWithClientAndEvent(
                BookingStatus.CANCELED, LocalDateTime.now().minusMonths(2));

        TicketReservation newCanceled = createReservationWithClientAndEvent(
                BookingStatus.CANCELED, LocalDateTime.now().minusDays(10));

        TicketReservation oldConfirmed = createReservationWithClientAndEvent(
                BookingStatus.CONFIRMED, LocalDateTime.now().minusMonths(2));

        int deletedCount = adminService.cleanupOldCanceledReservations();

        ticketReservationRepository.flush();
        entityManager.clear();

        assertEquals(1, deletedCount);
        assertFalse(ticketReservationRepository.findById(oldCanceled.getId()).isPresent());
        assertTrue(ticketReservationRepository.findById(newCanceled.getId()).isPresent());
        assertTrue(ticketReservationRepository.findById(oldConfirmed.getId()).isPresent());
    }

    @Test
    void cleanupOldCanceledReservations_WithMultipleOldCanceled_ShouldDeleteAll() {
        for (int i = 0; i < 5; i++) {
            createReservationWithClientAndEvent(
                    BookingStatus.CANCELED, LocalDateTime.now().minusMonths(2));
        }

        int deletedCount = adminService.cleanupOldCanceledReservations();

        ticketReservationRepository.flush();
        entityManager.clear();

        assertEquals(5, deletedCount);
        assertEquals(0, ticketReservationRepository.findByBookingStatus(BookingStatus.CANCELED).size());
    }

    @Test
    void cleanupOldCanceledReservations_WithNoOldCanceled_ShouldDeleteNothing() {
        TicketReservation newCanceled = createReservationWithClientAndEvent(
                BookingStatus.CANCELED, LocalDateTime.now().minusDays(10));

        int deletedCount = adminService.cleanupOldCanceledReservations();

        ticketReservationRepository.flush();
        entityManager.clear();

        assertEquals(0, deletedCount);
        assertTrue(ticketReservationRepository.findById(newCanceled.getId()).isPresent());
    }
}