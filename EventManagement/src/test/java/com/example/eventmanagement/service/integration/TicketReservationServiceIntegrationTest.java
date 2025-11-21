package com.example.eventmanagement.service.integration;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.repository.TicketReservationRepository;
import com.example.eventmanagement.service.ClientService;
import com.example.eventmanagement.service.EventService;
import com.example.eventmanagement.service.TicketReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TicketReservationServiceIntegrationTest {

    @Autowired
    private TicketReservationService ticketReservationService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private EventService eventService;

    @Autowired
    private TicketReservationRepository ticketReservationRepository;

    // Тесты остаются такими же как выше, но без @BeforeEach
    @Test
    void createReservation_WithValidData_SavesToDatabase() {
        // Arrange
        ClientCreateWithDependenciesDto clientDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );
        ClientDoneDto client = clientService.createClient(clientDto);

        EventCreateDto eventDto = new EventCreateDto(
                "Концерт", LocalDate.now().plusDays(10), 100,
                BigDecimal.valueOf(1000), EventStatus.PLANNED, "Описание"
        );
        EventDoneDto event = eventService.createEvent(eventDto);

        TicketReservationCreateDto reservationDto = new TicketReservationCreateDto(
                client.id(), event.id(), 2, BookingStatus.PENDING_CONFIRMATION
        );

        // Act
        TicketReservationDoneDto result = ticketReservationService.createReservation(reservationDto);

        // Assert
        assertNotNull(result.id());
        assertEquals(2, result.numberOfTickets());
        assertEquals(BookingStatus.PENDING_CONFIRMATION, result.bookingStatus());

        TicketReservation savedReservation = ticketReservationRepository.findById(result.id()).orElseThrow();
        assertEquals(client.id(), savedReservation.getClient().getId());
        assertEquals(event.id(), savedReservation.getEvent().getId());
    }

    @Test
    void confirmReservation_UpdatesStatusInDatabase() {
        // Arrange
        ClientCreateWithDependenciesDto clientDto = new ClientCreateWithDependenciesDto(
                "Петр Петров", "+79123456780", "petr@mail.ru",
                new PassportCreateDto("1235", "567891")
        );
        ClientDoneDto client = clientService.createClient(clientDto);

        EventCreateDto eventDto = new EventCreateDto(
                "Выставка", LocalDate.now().plusDays(15), 50,
                BigDecimal.valueOf(500), EventStatus.PLANNED, "Описание"
        );
        EventDoneDto event = eventService.createEvent(eventDto);

        TicketReservationCreateDto reservationDto = new TicketReservationCreateDto(
                client.id(), event.id(), 3, BookingStatus.PENDING_CONFIRMATION
        );
        TicketReservationDoneDto reservation = ticketReservationService.createReservation(reservationDto);

        // Act
        ticketReservationService.confirmReservation(reservation.id());

        // Assert
        TicketReservation confirmedReservation = ticketReservationRepository.findById(reservation.id()).orElseThrow();
        assertEquals(BookingStatus.CONFIRMED, confirmedReservation.getBookingStatus());
    }

    @Test
    void cancelReservation_UpdatesStatusInDatabase() {
        // Arrange
        ClientCreateWithDependenciesDto clientDto = new ClientCreateWithDependenciesDto(
                "Сергей Сергеев", "+79123456781", "sergey@mail.ru",
                new PassportCreateDto("1236", "567892")
        );
        ClientDoneDto client = clientService.createClient(clientDto);

        EventCreateDto eventDto = new EventCreateDto(
                "Семинар", LocalDate.now().plusDays(20), 30,
                BigDecimal.valueOf(300), EventStatus.PLANNED, "Описание"
        );
        EventDoneDto event = eventService.createEvent(eventDto);

        TicketReservationCreateDto reservationDto = new TicketReservationCreateDto(
                client.id(), event.id(), 1, BookingStatus.PENDING_CONFIRMATION
        );
        TicketReservationDoneDto reservation = ticketReservationService.createReservation(reservationDto);

        // Act
        ticketReservationService.cancelReservation(reservation.id());

        // Assert
        TicketReservation canceledReservation = ticketReservationRepository.findById(reservation.id()).orElseThrow();
        assertEquals(BookingStatus.CANCELED, canceledReservation.getBookingStatus());
    }
}