package com.example.eventmanagement.controller.integration;

import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.repository.TicketReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class AdminControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TicketReservationRepository ticketReservationRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/admin";
        ticketReservationRepository.deleteAll();
    }

    @Test
    void cleanupOldCanceledReservations_WhenReservationsExist_ShouldReturnSuccessMessage() {
        TicketReservation oldCanceled1 = TicketReservation.createForTesting(
                2, BookingStatus.CANCELED,
                LocalDateTime.now().minusMonths(2),
                LocalDateTime.now().minusMonths(2)
        );
        TicketReservation oldCanceled2 = TicketReservation.createForTesting(
                1, BookingStatus.CANCELED,
                LocalDateTime.now().minusMonths(3),
                LocalDateTime.now().minusMonths(3)
        );
        TicketReservation recentCanceled = TicketReservation.createForTesting(
                3, BookingStatus.CANCELED,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(10)
        );

        ticketReservationRepository.save(oldCanceled1);
        ticketReservationRepository.save(oldCanceled2);
        ticketReservationRepository.save(recentCanceled);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/cleanup/canceled-reservations",
                null,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Удалено 2 старых отмененных бронирований", response.getBody());

        assertEquals(1, ticketReservationRepository.count());
        assertTrue(ticketReservationRepository.findById(recentCanceled.getId()).isPresent());
    }

    @Test
    void cleanupOldCanceledReservations_WhenNoReservations_ShouldReturnNoDeletionsMessage() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/cleanup/canceled-reservations",
                null,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Нет старых отмененных бронирований для очистки", response.getBody());
        assertEquals(0, ticketReservationRepository.count());
    }

    @Test
    void cleanupOldCanceledReservations_WhenOnlyRecentCanceled_ShouldReturnNoDeletionsMessage() {
        TicketReservation recentCanceled1 = TicketReservation.createForTesting(
                2, BookingStatus.CANCELED,
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(20)
        );
        TicketReservation recentCanceled2 = TicketReservation.createForTesting(
                1, BookingStatus.CANCELED,
                LocalDateTime.now().minusDays(15),
                LocalDateTime.now().minusDays(15)
        );

        ticketReservationRepository.save(recentCanceled1);
        ticketReservationRepository.save(recentCanceled2);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/cleanup/canceled-reservations",
                null,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Нет старых отмененных бронирований для очистки", response.getBody());
        assertEquals(2, ticketReservationRepository.count());
    }

    @Test
    void cleanupOldCanceledReservations_WithMixedStatuses_ShouldDeleteOnlyOldCanceled() {
        TicketReservation oldCanceled = TicketReservation.createForTesting(
                2, BookingStatus.CANCELED,
                LocalDateTime.now().minusMonths(2),
                LocalDateTime.now().minusMonths(2)
        );
        TicketReservation oldConfirmed = TicketReservation.createForTesting(
                1, BookingStatus.CONFIRMED,
                LocalDateTime.now().minusMonths(2),
                LocalDateTime.now().minusMonths(2)
        );
        TicketReservation oldPending = TicketReservation.createForTesting(
                3, BookingStatus.PENDING_CONFIRMATION,
                LocalDateTime.now().minusMonths(2),
                LocalDateTime.now().minusMonths(2)
        );

        ticketReservationRepository.save(oldCanceled);
        ticketReservationRepository.save(oldConfirmed);
        ticketReservationRepository.save(oldPending);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/cleanup/canceled-reservations",
                null,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Удалено 1 старых отмененных бронирований", response.getBody());
        assertEquals(2, ticketReservationRepository.count());
        assertFalse(ticketReservationRepository.findById(oldCanceled.getId()).isPresent());
        assertTrue(ticketReservationRepository.findById(oldConfirmed.getId()).isPresent());
        assertTrue(ticketReservationRepository.findById(oldPending.getId()).isPresent());
    }
}