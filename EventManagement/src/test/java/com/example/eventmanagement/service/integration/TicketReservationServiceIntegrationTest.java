package com.example.eventmanagement.service.integration;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.TicketReservationRepository;
import com.example.eventmanagement.service.ClientService;
import com.example.eventmanagement.service.EventService;
import com.example.eventmanagement.service.TicketReservationService;
import com.example.eventmanagement.testutils.TestDataHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void cleanupOldCanceledReservations_WhenOldCanceledReservationsExist_DeletesThem() {
        ClientCreateWithDependenciesDto clientDto = new ClientCreateWithDependenciesDto(
                "Алексей Алексеев", "+79123456782", "alex@mail.ru",
                new PassportCreateDto("1237", "567893")
        );
        ClientDoneDto clientDone = clientService.createClient(clientDto);

        EventCreateDto eventDto = new EventCreateDto(
                "Конференция", LocalDate.now().plusDays(25), 200,
                BigDecimal.valueOf(1500), EventStatus.PLANNED, "Описание"
        );
        EventDoneDto eventDone = eventService.createEvent(eventDto);

        // Получаем сущности из репозитория
        var client = clientRepository.findById(clientDone.id()).orElseThrow();
        var event = eventRepository.findById(eventDone.id()).orElseThrow();

        // Создаем старое отмененное бронирование с помощью TestDataHelper
        LocalDateTime oldDateTime = LocalDateTime.now().minusMonths(2);
        TicketReservation oldReservation = TestDataHelper.createOldCanceledReservation(
                client, event, oldDateTime
        );

        // Сохраняем бронирование
        TicketReservation savedReservation = ticketReservationRepository.save(oldReservation);

        // Проверяем, что дата действительно старая
        assertTrue(savedReservation.getUpdatedAt().isBefore(LocalDateTime.now().minusMonths(1)));

        int deletedCount = ticketReservationService.cleanupOldCanceledReservations();

        assertEquals(1, deletedCount);
        assertFalse(ticketReservationRepository.findById(savedReservation.getId()).isPresent());
    }

    @Test
    void cleanupOldCanceledReservations_WhenNoOldCanceledReservations_ReturnsZero() {
        ClientCreateWithDependenciesDto clientDto = new ClientCreateWithDependenciesDto(
                "Михаил Михайлов", "+79123456783", "mikhail@mail.ru",
                new PassportCreateDto("1238", "567894")
        );
        ClientDoneDto client = clientService.createClient(clientDto);

        EventCreateDto eventDto = new EventCreateDto(
                "Фестиваль", LocalDate.now().plusDays(30), 300,
                BigDecimal.valueOf(2000), EventStatus.PLANNED, "Описание"
        );
        EventDoneDto event = eventService.createEvent(eventDto);

        TicketReservationCreateDto reservationDto = new TicketReservationCreateDto(
                client.id(), event.id(), 5, BookingStatus.PENDING_CONFIRMATION
        );
        TicketReservationDoneDto reservation = ticketReservationService.createReservation(reservationDto);

        // Отменяем бронирование (оно будет иметь текущую дату обновления)
        ticketReservationService.cancelReservation(reservation.id());

        int deletedCount = ticketReservationService.cleanupOldCanceledReservations();

        assertEquals(0, deletedCount);
        assertTrue(ticketReservationRepository.findById(reservation.id()).isPresent());
    }

    @Test
    void cleanupOldCanceledReservations_WhenMixedReservations_DeletesOnlyOldCanceled() {
        ClientCreateWithDependenciesDto clientDto = new ClientCreateWithDependenciesDto(
                "Дмитрий Дмитриев", "+79123456784", "dmitry@mail.ru",
                new PassportCreateDto("1239", "567895")
        );
        ClientDoneDto clientDone = clientService.createClient(clientDto);

        EventCreateDto eventDto = new EventCreateDto(
                "Выставка картин", LocalDate.now().plusDays(35), 150,
                BigDecimal.valueOf(800), EventStatus.PLANNED, "Описание"
        );
        EventDoneDto eventDone = eventService.createEvent(eventDto);

        // Получаем сущности
        var client = clientRepository.findById(clientDone.id()).orElseThrow();
        var event = eventRepository.findById(eventDone.id()).orElseThrow();

        // 1. Старое отмененное бронирование (создаем через TestDataHelper)
        LocalDateTime oldDateTime = LocalDateTime.now().minusMonths(2);
        TicketReservation oldCanceled = TestDataHelper.createOldCanceledReservation(
                client, event, oldDateTime
        );
        TicketReservation savedOldCanceled = ticketReservationRepository.save(oldCanceled);

        // 2. Новое отмененное бронирование (через сервис)
        TicketReservationCreateDto reservation2Dto = new TicketReservationCreateDto(
                clientDone.id(), eventDone.id(), 3, BookingStatus.PENDING_CONFIRMATION
        );
        TicketReservationDoneDto newCanceledReservation = ticketReservationService.createReservation(reservation2Dto);
        ticketReservationService.cancelReservation(newCanceledReservation.id());

        // 3. Подтвержденное бронирование (через сервис)
        TicketReservationCreateDto reservation3Dto = new TicketReservationCreateDto(
                clientDone.id(), eventDone.id(), 1, BookingStatus.PENDING_CONFIRMATION
        );
        TicketReservationDoneDto confirmedReservation = ticketReservationService.createReservation(reservation3Dto);
        ticketReservationService.confirmReservation(confirmedReservation.id());

        int deletedCount = ticketReservationService.cleanupOldCanceledReservations();

        assertEquals(1, deletedCount);
        assertFalse(ticketReservationRepository.findById(savedOldCanceled.getId()).isPresent()); // Удалено
        assertTrue(ticketReservationRepository.findById(newCanceledReservation.id()).isPresent());  // Не удалено
        assertTrue(ticketReservationRepository.findById(confirmedReservation.id()).isPresent());  // Не удалено
    }

    @Test
    void cleanupOldCanceledReservations_WhenReservationIsPending_NotDeleted() {
        ClientCreateWithDependenciesDto clientDto = new ClientCreateWithDependenciesDto(
                "Сергей Сергеев", "+79123456785", "sergey@mail.ru",
                new PassportCreateDto("1240", "567896")
        );
        ClientDoneDto clientDone = clientService.createClient(clientDto);

        EventCreateDto eventDto = new EventCreateDto(
                "Семинар", LocalDate.now().plusDays(40), 100,
                BigDecimal.valueOf(400), EventStatus.PLANNED, "Описание"
        );
        EventDoneDto eventDone = eventService.createEvent(eventDto);

        var client = clientRepository.findById(clientDone.id()).orElseThrow();
        var event = eventRepository.findById(eventDone.id()).orElseThrow();

        // Создаем ожидающее бронирование со старыми датами
        LocalDateTime oldDateTime = LocalDateTime.now().minusMonths(2);
        TicketReservation pendingReservation = TestDataHelper.createReservationWithCustomDates(
                client, event, 2, BookingStatus.PENDING_CONFIRMATION, oldDateTime, oldDateTime
        );
        TicketReservation savedPending = ticketReservationRepository.save(pendingReservation);

        int deletedCount = ticketReservationService.cleanupOldCanceledReservations();

        assertEquals(0, deletedCount);
        assertTrue(ticketReservationRepository.findById(savedPending.getId()).isPresent());
    }

    @Test
    void cleanupOldCanceledReservations_WhenReservationIsConfirmed_NotDeleted() {
        ClientCreateWithDependenciesDto clientDto = new ClientCreateWithDependenciesDto(
                "Андрей Андреев", "+79123456786", "andrey@mail.ru",
                new PassportCreateDto("1241", "567897")
        );
        ClientDoneDto clientDone = clientService.createClient(clientDto);

        EventCreateDto eventDto = new EventCreateDto(
                "Мастер-класс", LocalDate.now().plusDays(45), 80,
                BigDecimal.valueOf(600), EventStatus.PLANNED, "Описание"
        );
        EventDoneDto eventDone = eventService.createEvent(eventDto);

        var client = clientRepository.findById(clientDone.id()).orElseThrow();
        var event = eventRepository.findById(eventDone.id()).orElseThrow();

        LocalDateTime oldDateTime = LocalDateTime.now().minusMonths(2);
        TicketReservation confirmedReservation = TestDataHelper.createReservationWithCustomDates(
                client, event, 2, BookingStatus.CONFIRMED, oldDateTime, oldDateTime
        );
        TicketReservation savedConfirmed = ticketReservationRepository.save(confirmedReservation);

        int deletedCount = ticketReservationService.cleanupOldCanceledReservations();

        assertEquals(0, deletedCount);
        assertTrue(ticketReservationRepository.findById(savedConfirmed.getId()).isPresent());
    }

    @Test
    void cleanupOldCanceledReservations_BoundaryTest_ExactlyOneMonthOld_NotDeleted() {
        ClientCreateWithDependenciesDto clientDto = new ClientCreateWithDependenciesDto(
                "Олег Олегов", "+79123456787", "oleg@mail.ru",
                new PassportCreateDto("1242", "567898")
        );
        ClientDoneDto clientDone = clientService.createClient(clientDto);

        EventCreateDto eventDto = new EventCreateDto(
                "Воркшоп", LocalDate.now().plusDays(50), 120,
                BigDecimal.valueOf(700), EventStatus.PLANNED, "Описание"
        );
        EventDoneDto eventDone = eventService.createEvent(eventDto);

        var client = clientRepository.findById(clientDone.id()).orElseThrow();
        var event = eventRepository.findById(eventDone.id()).orElseThrow();

        // Создаем отмененное бронирование, которому ровно 30 дней (не старее месяца)
        LocalDateTime notSoOldDate = LocalDateTime.now().minusDays(20);
        TicketReservation boundaryReservation = TestDataHelper.createOldCanceledReservation(
                client, event, notSoOldDate
        );
        TicketReservation savedBoundary = ticketReservationRepository.save(boundaryReservation);

        int deletedCount = ticketReservationService.cleanupOldCanceledReservations();

        assertEquals(0, deletedCount);
        assertTrue(ticketReservationRepository.findById(savedBoundary.getId()).isPresent());
    }

    @Test
    void cleanupOldCanceledReservations_BoundaryTest_OneMonthAndOneDayOld_Deleted() {
        ClientCreateWithDependenciesDto clientDto = new ClientCreateWithDependenciesDto(
                "Николай Николаев", "+79123456788", "nikolay@mail.ru",
                new PassportCreateDto("1243", "567899")
        );
        ClientDoneDto clientDone = clientService.createClient(clientDto);

        EventCreateDto eventDto = new EventCreateDto(
                "Круглый стол", LocalDate.now().plusDays(55), 90,
                BigDecimal.valueOf(900), EventStatus.PLANNED, "Описание"
        );
        EventDoneDto eventDone = eventService.createEvent(eventDto);

        var client = clientRepository.findById(clientDone.id()).orElseThrow();
        var event = eventRepository.findById(eventDone.id()).orElseThrow();

        LocalDateTime oneMonthAndOneDayAgo = LocalDateTime.now().minusMonths(1).minusDays(1);
        TicketReservation oldReservation = TestDataHelper.createOldCanceledReservation(
                client, event, oneMonthAndOneDayAgo
        );
        TicketReservation savedOld = ticketReservationRepository.save(oldReservation);

        int deletedCount = ticketReservationService.cleanupOldCanceledReservations();

        assertEquals(1, deletedCount);
        assertFalse(ticketReservationRepository.findById(savedOld.getId()).isPresent());
    }

    @Test
    void deleteCanceledReservation_Success() {
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
        TicketReservationDoneDto reservation = ticketReservationService.createReservation(reservationDto);

        // Отменяем бронирование
        ticketReservationService.cancelReservation(reservation.id());

        ticketReservationService.deleteCanceledReservation(reservation.id());

        assertFalse(ticketReservationRepository.findById(reservation.id()).isPresent());
    }

    @Test
    void deleteCanceledReservation_WhenReservationNotCanceled_ThrowsException() {
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

        assertThrows(com.example.eventmanagement.exception.BusinessValidationException.class,
                () -> ticketReservationService.deleteCanceledReservation(reservation.id()));

        assertTrue(ticketReservationRepository.findById(reservation.id()).isPresent());
    }
}