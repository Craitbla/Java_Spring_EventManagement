package com.example.eventmanagement.service.integration;

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
import com.example.eventmanagement.service.AdminService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
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

    @PersistenceContext
    private EntityManager entityManager;

    private Client testClient;
    private Event testEvent;
    private Passport testPassport;

    @BeforeEach
    void setUp() {
        testPassport = passportRepository.save(new Passport("1234", "567890"));
        testClient = clientRepository.save(new Client("Тестовый Клиент", "+71234567890", "test@example.com", testPassport));

        testEvent = eventRepository.save(new Event("Тестовое мероприятие",
                LocalDate.now().plusDays(10),
                100,
                BigDecimal.valueOf(1000.00),
                EventStatus.PLANNED,
                "Тестовое описание"));
    }

    @AfterEach
    void tearDown() {
        ticketReservationRepository.deleteAll();
        clientRepository.deleteAll();
        eventRepository.deleteAll();
        passportRepository.deleteAll();
    }

    @Test
    @Transactional
    void cleanupOldCanceledReservations_shouldDeleteOldCanceledReservations() {
        createReservation(2, BookingStatus.CANCELED, LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusMonths(1));
        createReservation(1, BookingStatus.CANCELED, LocalDateTime.now().minusMonths(3), LocalDateTime.now().minusMonths(2));

        assertThat(ticketReservationRepository.findAll()).hasSize(2);

        int deletedCount = adminService.cleanupOldCanceledReservations();

        assertThat(deletedCount).isEqualTo(2);
        assertThat(ticketReservationRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void cleanupOldCanceledReservations_shouldNotDeleteRecentCanceledReservations() {
        createReservation(3, BookingStatus.CANCELED, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5));

        assertThat(ticketReservationRepository.findAll()).hasSize(1);

        int deletedCount = adminService.cleanupOldCanceledReservations();

        assertThat(deletedCount).isEqualTo(0);
        assertThat(ticketReservationRepository.findAll()).hasSize(1);
        assertThat(ticketReservationRepository.findAll().get(0).getBookingStatus()).isEqualTo(BookingStatus.CANCELED);
    }

    @Test
    @Transactional
    void cleanupOldCanceledReservations_shouldNotDeleteOldNonCanceledReservations() {
        createReservation(2, BookingStatus.CONFIRMED, LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusMonths(1));
        createReservation(1, BookingStatus.PENDING_CONFIRMATION, LocalDateTime.now().minusMonths(3), LocalDateTime.now().minusMonths(2));

        assertThat(ticketReservationRepository.findAll()).hasSize(2);

        int deletedCount = adminService.cleanupOldCanceledReservations();

        assertThat(deletedCount).isEqualTo(0);
        List<TicketReservation> allReservations = ticketReservationRepository.findAll();
        assertThat(allReservations).hasSize(2);

        List<BookingStatus> statuses = allReservations.stream()
                .map(TicketReservation::getBookingStatus)
                .toList();

        assertThat(statuses).contains(BookingStatus.CONFIRMED, BookingStatus.PENDING_CONFIRMATION);
    }

    @Test
    @Transactional
    void cleanupOldCanceledReservations_shouldDeleteOnlyOldCanceledReservationsWhenMixedData() {
        // Старые отмененные (должны быть удалены)
        createReservation(2, BookingStatus.CANCELED, LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusMonths(1));
        createReservation(1, BookingStatus.CANCELED, LocalDateTime.now().minusMonths(3), LocalDateTime.now().minusMonths(2));

        // Свежие отмененные (должны остаться)
        createReservation(3, BookingStatus.CANCELED, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5));

        // Старые не отмененные (должны остаться)
        createReservation(2, BookingStatus.CONFIRMED, LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusMonths(1));

        assertThat(ticketReservationRepository.findAll()).hasSize(4);

        int deletedCount = adminService.cleanupOldCanceledReservations();

        assertThat(deletedCount).isEqualTo(2);

        List<TicketReservation> remainingReservations = ticketReservationRepository.findAll();
        assertThat(remainingReservations).hasSize(2);

        List<BookingStatus> remainingStatuses = remainingReservations.stream()
                .map(TicketReservation::getBookingStatus)
                .toList();

        assertThat(remainingStatuses).containsExactlyInAnyOrder(
                BookingStatus.CANCELED,
                BookingStatus.CONFIRMED
        );
    }

    @Test
    @Transactional
    void cleanupOldCanceledReservations_shouldReturnZeroWhenNoReservationsToDelete() {
        int deletedCount = adminService.cleanupOldCanceledReservations();
        assertThat(deletedCount).isEqualTo(0);
    }

    private void createReservation(int numberOfTickets, BookingStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
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