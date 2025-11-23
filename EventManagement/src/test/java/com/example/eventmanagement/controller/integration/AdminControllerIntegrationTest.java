package com.example.eventmanagement.controller.integration;

import com.example.eventmanagement.entity.*;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    void cleanupOldCanceledReservations_WhenReservationsExist_ShouldReturnSuccessMessage() throws Exception {
        createReservationWithClientAndEvent(BookingStatus.CANCELED, LocalDateTime.now().minusMonths(2));

        mockMvc.perform(post("/api/admin/cleanup/canceled-reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(1))
                .andExpect(jsonPath("$.message").value("Удалено 1 старых отмененных бронирований"));
    }

    @Test
    void cleanupOldCanceledReservations_WhenNoReservations_ShouldReturnNoDeletionsMessage() throws Exception {
        mockMvc.perform(post("/api/admin/cleanup/canceled-reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(0))
                .andExpect(jsonPath("$.message").value("Нет старых отмененных бронирований для очистки"));
    }
}