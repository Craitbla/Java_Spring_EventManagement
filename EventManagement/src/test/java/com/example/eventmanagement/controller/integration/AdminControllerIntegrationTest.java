package com.example.eventmanagement.controller.integration;

import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.PassportRepository;
import com.example.eventmanagement.repository.TicketReservationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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

    @PersistenceContext
    private EntityManager entityManager;

    private Client testClient;
    private Event testEvent;
    private Passport testPassport;

    @BeforeEach
    void setUp() {
        // Create and save passport first
        testPassport = new Passport("1234", "567890");
        testPassport = passportRepository.save(testPassport);

        // Create client with the saved passport
        testClient = new Client("Тестовый Клиент", "+71234567890", "test@example.com", testPassport);
        testClient = clientRepository.save(testClient);

        // Create and save event
        testEvent = new Event("Тестовое мероприятие",
                LocalDate.now().plusDays(10),
                100,
                BigDecimal.valueOf(1000.00),
                EventStatus.PLANNED,
                "Тестовое описание");
        testEvent = eventRepository.save(testEvent);
    }

    @AfterEach
    void tearDown() {
        ticketReservationRepository.deleteAll();
        clientRepository.deleteAll();
        eventRepository.deleteAll();
        passportRepository.deleteAll();
    }

    @Test
    void cleanupOldCanceledReservations_shouldDeleteOldCanceledReservations() throws Exception {
        // Создаем старые отмененные бронирования
        createAndSaveReservation(2, BookingStatus.CANCELED, LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusMonths(1));
        createAndSaveReservation(1, BookingStatus.CANCELED, LocalDateTime.now().minusMonths(3), LocalDateTime.now().minusMonths(2));

        assertThat(ticketReservationRepository.findAll()).hasSize(2);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/cleanup/canceled-reservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(2))
                .andExpect(jsonPath("$.message").value("Удалено 2 бронирований"));

        assertThat(ticketReservationRepository.findAll()).isEmpty();
    }

    @Test
    void cleanupOldCanceledReservations_shouldReturnNoReservationsMessageWhenNoneToClean() throws Exception {
        // Создаем свежее отмененное бронирование (не должно удаляться)
        createAndSaveReservation(3, BookingStatus.CANCELED, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5));

        // Создаем подтвержденное бронирование (не должно удаляться)
        createAndSaveReservation(2, BookingStatus.CONFIRMED, LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusMonths(1));

        assertThat(ticketReservationRepository.findAll()).hasSize(2);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/cleanup/canceled-reservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(0))
                .andExpect(jsonPath("$.message").value("Нет старых отмененных бронирований для очистки"));

        assertThat(ticketReservationRepository.findAll()).hasSize(2);
    }

    private void createAndSaveReservation(int numberOfTickets, BookingStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        // Создаем бронирование
        TicketReservation reservation = new TicketReservation(numberOfTickets, status);

        // Устанавливаем связи через публичные методы
        testClient.addTicketReservation(reservation);
        testEvent.addTicketReservation(reservation);

        // Сохраняем бронирование
        reservation = ticketReservationRepository.save(reservation);

        // Обновляем временные метки напрямую в базе данных
        updateReservationTimestamps(reservation.getId(), createdAt, updatedAt);

        // Перезагружаем сущность для гарантии
        ticketReservationRepository.flush();
        entityManager.clear();
    }

    private void updateReservationTimestamps(Long reservationId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        String sql = "UPDATE ticket_reservations SET created_at = ?, updated_at = ? WHERE id = ?";
        var query = entityManager.createNativeQuery(sql);
        query.setParameter(1, Timestamp.from(createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        query.setParameter(2, Timestamp.from(updatedAt.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        query.setParameter(3, reservationId);
        query.executeUpdate();
    }
}