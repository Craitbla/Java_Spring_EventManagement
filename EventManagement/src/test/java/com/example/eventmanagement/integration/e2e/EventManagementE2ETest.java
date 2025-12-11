package com.example.eventmanagement.integration.e2e;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.enums.EventStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventManagementE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    // ===========================
    // 1. Основной happy-path флоу
    // ===========================

    @Test
    @DisplayName("Полный happy-path: клиент + событие + бронь + подтверждение + статистика + запрет удаления клиента")
    void fullFlow_createClientEventReservationAndConfirm() {
        // 1. Создаём клиента
        PassportCreateDto passport = new PassportCreateDto("1234", "567890");
        ClientCreateWithDependenciesDto clientReq = new ClientCreateWithDependenciesDto(
                "Иванов Иван Иванович",
                "+79991234567",
                "ivan.e2e@example.com",
                passport
        );

        ResponseEntity<ClientDoneDto> clientResp = restTemplate.postForEntity(
                "/api/clients",
                clientReq,
                ClientDoneDto.class
        );

        assertThat(clientResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(clientResp.getBody()).isNotNull();
        ClientDoneDto createdClient = clientResp.getBody();
        Long clientId = createdClient.id();

        // 2. Создаём событие
        LocalDate eventDate = LocalDate.now().plusDays(10);
        EventCreateDto eventReq = new EventCreateDto(
                "E2E Конференция по Java",
                eventDate,
                100,
                new BigDecimal("1500.00"),
                "E2E тестовое мероприятие"
        );

        ResponseEntity<EventDoneDto> eventResp = restTemplate.postForEntity(
                "/api/events",
                eventReq,
                EventDoneDto.class
        );

        assertThat(eventResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(eventResp.getBody()).isNotNull();
        EventDoneDto createdEvent = eventResp.getBody();
        Long eventId = createdEvent.id();

        assertThat(createdEvent.status()).isEqualTo(EventStatus.PLANNED);

        // 3. Создаём бронирование
        TicketReservationCreateDto reservationReq = new TicketReservationCreateDto(
                clientId,
                eventId,
                2,
                BookingStatus.PENDING_CONFIRMATION
        );

        ResponseEntity<TicketReservationDoneDto> reservationResp = restTemplate.postForEntity(
                "/api/ticketReservations",
                reservationReq,
                TicketReservationDoneDto.class
        );

        assertThat(reservationResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reservationResp.getBody()).isNotNull();
        TicketReservationDoneDto createdReservation = reservationResp.getBody();
        Long reservationId = createdReservation.id();

        assertThat(createdReservation.bookingStatus()).isEqualTo(BookingStatus.PENDING_CONFIRMATION);
        assertThat(createdReservation.numberOfTickets()).isEqualTo(2);

        // 3.1. Получаем бронь по ID
        ResponseEntity<TicketReservationDoneDto> getReservationResp = restTemplate.getForEntity(
                "/api/ticketReservations/{id}",
                TicketReservationDoneDto.class,
                reservationId
        );
        assertThat(getReservationResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getReservationResp.getBody()).isNotNull();
        assertThat(getReservationResp.getBody().id()).isEqualTo(reservationId);

        // 4. Подтверждаем бронирование
        ResponseEntity<TicketReservationDoneDto> confirmResp = restTemplate.exchange(
                "/api/ticketReservations/{id}/confirm",
                HttpMethod.PUT,
                null,
                TicketReservationDoneDto.class,
                reservationId
        );

        assertThat(confirmResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(confirmResp.getBody()).isNotNull();
        TicketReservationDoneDto confirmedReservation = confirmResp.getBody();
        assertThat(confirmedReservation.bookingStatus()).isEqualTo(BookingStatus.CONFIRMED);

        // 5. Проверяем статистику мероприятия
        ResponseEntity<EventStatisticsDto> statsResp = restTemplate.getForEntity(
                "/api/events/{id}/statistics",
                EventStatisticsDto.class,
                eventId
        );

        assertThat(statsResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statsResp.getBody()).isNotNull();
        EventStatisticsDto stats = statsResp.getBody();

        assertThat(stats.id()).isEqualTo(eventId);
        assertThat(stats.confirmedTickets()).isEqualTo(2);
        assertThat(stats.totalRevenue()).isEqualByComparingTo(new BigDecimal("3000.00"));

        // 6. Получаем список бронирований
        ResponseEntity<TicketReservationDto[]> listResp = restTemplate.getForEntity(
                "/api/ticketReservations",
                TicketReservationDto[].class
        );
        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResp.getBody()).isNotNull();
        assertThat(listResp.getBody().length).isGreaterThanOrEqualTo(1);

        // 7. Проверяем, что клиента с активной бронью удалить нельзя
        ResponseEntity<GlobalError> deleteClientResp = restTemplate.exchange(
                "/api/clients/{id}",
                HttpMethod.DELETE,
                null,
                GlobalError.class,
                clientId
        );

        assertThat(deleteClientResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(deleteClientResp.getBody()).isNotNull();
        assertThat(deleteClientResp.getBody().error()).isEqualTo("BUSINESS_RULE_ERROR");
    }

    // ==================================
    // 2. Валидация клиента при создании
    // ==================================

    @Test
    @DisplayName("Валидация клиента: неправильный телефон → 400 VALIDATION_ERROR")
    void createClient_invalidPhone_shouldReturnValidationError() {
        PassportCreateDto passport = new PassportCreateDto("1234", "567890");

        ClientCreateWithDependenciesDto clientReq = new ClientCreateWithDependenciesDto(
                "Плохой Телефон",
                "89991234567", // без +7
                "badphone.e2e@example.com",
                passport
        );

        ResponseEntity<GlobalError> resp = restTemplate.postForEntity(
                "/api/clients",
                clientReq,
                GlobalError.class
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().error()).isEqualTo("VALIDATION_ERROR");
        assertThat(resp.getBody().message()).contains("phoneNumber");
    }

    // ==========================
    // 3. Клиенты: CRUD и поиск
    // ==========================

    @Test
    @DisplayName("Клиенты: getAll, getById, search, update, replace passport, delete без броней")
    void clients_crudAndSearchFlow() {
        // Создаём клиента 1
        ClientDoneDto c1 = createClient(
                "Клиент Один",
                "+79990000001",
                "client1.e2e@example.com",
                "1111",
                "111111"
        );

        // Создаём клиента 2
        ClientDoneDto c2 = createClient(
                "Клиент Два",
                "+79990000002",
                "client2.e2e@example.com",
                "2222",
                "222222"
        );

        // GET /api/clients
        ResponseEntity<ClientDoneDto[]> allResp = restTemplate.getForEntity(
                "/api/clients",
                ClientDoneDto[].class
        );
        assertThat(allResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(allResp.getBody()).isNotNull();
        assertThat(allResp.getBody().length).isGreaterThanOrEqualTo(2);

        // GET /api/clients/{id}
        ResponseEntity<ClientDoneDto> byIdResp = restTemplate.getForEntity(
                "/api/clients/{id}",
                ClientDoneDto.class,
                c1.id()
        );
        assertThat(byIdResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(byIdResp.getBody()).isNotNull();
        assertThat(byIdResp.getBody().email()).isEqualTo("client1.e2e@example.com");

        // GET /api/clients/search?searchTerm=
        ResponseEntity<ClientDoneDto[]> searchResp = restTemplate.getForEntity(
                "/api/clients/search?searchTerm={term}",
                ClientDoneDto[].class,
                "Клиент"
        );
        assertThat(searchResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResp.getBody()).isNotNull();
        assertThat(searchResp.getBody().length).isGreaterThanOrEqualTo(2);

        // PUT /api/clients/{id} (обновление ФИО/тел/почты)
        ClientCreateDto updateReq = new ClientCreateDto(
                "Клиент Один Обновлённый",
                "+79990000011",
                "client1.updated.e2e@example.com"
        );
        ResponseEntity<ClientDoneDto> updateResp = restTemplate.exchange(
                "/api/clients/{id}",
                HttpMethod.PUT,
                new HttpEntity<>(updateReq),
                ClientDoneDto.class,
                c1.id()
        );

        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResp.getBody()).isNotNull();
        assertThat(updateResp.getBody().fullName()).contains("Обновлённый");
        assertThat(updateResp.getBody().phoneNumber()).isEqualTo("+79990000011");

        // PUT /api/clients/{id}/passport (замена паспорта)
        PassportCreateDto newPassport = new PassportCreateDto("9999", "999999");
        ResponseEntity<ClientDoneDto> passportResp = restTemplate.exchange(
                "/api/clients/{id}/passport",
                HttpMethod.PUT,
                new HttpEntity<>(newPassport),
                ClientDoneDto.class,
                c1.id()
        );

        assertThat(passportResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(passportResp.getBody()).isNotNull();
        assertThat(passportResp.getBody().passport().series()).isEqualTo("9999");
        assertThat(passportResp.getBody().passport().number()).isEqualTo("999999");

        // DELETE /api/clients/{id} (у клиента 2 нет бронирований, должен удалиться)
        ResponseEntity<Void> deleteResp = restTemplate.exchange(
                "/api/clients/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                c2.id()
        );
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Проверяем, что теперь 404 при GET
        ResponseEntity<GlobalError> getDeletedResp = restTemplate.getForEntity(
                "/api/clients/{id}",
                GlobalError.class,
                c2.id()
        );
        assertThat(getDeletedResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getDeletedResp.getBody()).isNotNull();
        assertThat(getDeletedResp.getBody().error()).isEqualTo("NOT_FOUND");
    }

    // ==========================
    // 4. Мероприятия: статусы и удаление
    // ==========================
    @Test
    @DisplayName("Мероприятия: валидные и невалидные переходы статуса, getAll, delete")
    void events_statusTransitionsAndDelete() {
        // Создаём событие
        EventDoneDto event = createEvent(
                "E2E Статусы События",
                LocalDate.now().plusDays(20),
                50,
                new BigDecimal("2000.00"),
                "Событие для проверки статусов"
        );
        Long eventId = event.id();

        // GET /api/events/{id}
        ResponseEntity<EventDoneDto> getByIdResp = restTemplate.getForEntity(
                "/api/events/{id}",
                EventDoneDto.class,
                eventId
        );
        assertThat(getByIdResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getByIdResp.getBody()).isNotNull();
        assertThat(getByIdResp.getBody().status()).isEqualTo(EventStatus.PLANNED);

        // ❌ Невалидный переход: PLANNED -> COMPLETED
        // Отправляем enum COMPLETED, Jackson сам сделает "завершено"
        HttpEntity<EventStatus> invalidBody = new HttpEntity<>(EventStatus.COMPLETED);
        ResponseEntity<GlobalError> invalidStatusResp = restTemplate.exchange(
                "/api/events/{id}/status",
                HttpMethod.PUT,
                invalidBody,
                GlobalError.class,
                eventId
        );

        assertThat(invalidStatusResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invalidStatusResp.getBody()).isNotNull();
        assertThat(invalidStatusResp.getBody().error()).isEqualTo("BUSINESS_RULE_ERROR");

        // ✅ Валидный переход: PLANNED -> ONGOING
        HttpEntity<EventStatus> ongoingBody = new HttpEntity<>(EventStatus.ONGOING);
        ResponseEntity<EventDoneDto> ongoingResp = restTemplate.exchange(
                "/api/events/{id}/status",
                HttpMethod.PUT,
                ongoingBody,
                EventDoneDto.class,
                eventId
        );

        assertThat(ongoingResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ongoingResp.getBody()).isNotNull();
        assertThat(ongoingResp.getBody().status()).isEqualTo(EventStatus.ONGOING);

        // ✅ Валидный переход: ONGOING -> COMPLETED
        HttpEntity<EventStatus> completedBody = new HttpEntity<>(EventStatus.COMPLETED);
        ResponseEntity<EventDoneDto> completedResp = restTemplate.exchange(
                "/api/events/{id}/status",
                HttpMethod.PUT,
                completedBody,
                EventDoneDto.class,
                eventId
        );

        assertThat(completedResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(completedResp.getBody()).isNotNull();
        assertThat(completedResp.getBody().status()).isEqualTo(EventStatus.COMPLETED);

        // GET /api/events
        ResponseEntity<EventDto[]> allResp = restTemplate.getForEntity(
                "/api/events",
                EventDto[].class
        );
        assertThat(allResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(allResp.getBody()).isNotNull();
        assertThat(allResp.getBody().length).isGreaterThanOrEqualTo(1);

        // DELETE /api/events/{id} (COMPLETED уже можно удалить)
        ResponseEntity<Void> deleteResp = restTemplate.exchange(
                "/api/events/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                eventId
        );
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Проверяем, что теперь GET вернёт 404
        ResponseEntity<GlobalError> getDeletedResp = restTemplate.getForEntity(
                "/api/events/{id}",
                GlobalError.class,
                eventId
        );
        assertThat(getDeletedResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getDeletedResp.getBody()).isNotNull();
        assertThat(getDeletedResp.getBody().error()).isEqualTo("NOT_FOUND");
    }


    // ======================================
    // 5. Бронирования: отмена и очистка
    // ======================================

    @Test
    @DisplayName("Бронирования: отмена, getAll, cleanup старых отменённых")
    void reservations_cancelAndCleanup() {
        // Создаём клиента
        ClientDoneDto client = createClient(
                "Клиент Для Отмены",
                "+79995550001",
                "cancel.e2e@example.com",
                "3333",
                "333333"
        );

        // Создаём событие (достаточно далеко в будущем, чтобы разрешить отмену за день)
        EventDoneDto event = createEvent(
                "E2E Событие для отмены",
                LocalDate.now().plusDays(30),
                10,
                new BigDecimal("1000.00"),
                "Событие для отмены резерваций"
        );

        // Создаём бронирование
        TicketReservationCreateDto reservationReq = new TicketReservationCreateDto(
                client.id(),
                event.id(),
                1,
                null   // пусть статус по умолчанию будет PENDING_CONFIRMATION
        );

        ResponseEntity<TicketReservationDoneDto> reservationResp = restTemplate.postForEntity(
                "/api/ticketReservations",
                reservationReq,
                TicketReservationDoneDto.class
        );

        assertThat(reservationResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reservationResp.getBody()).isNotNull();
        Long reservationId = reservationResp.getBody().id();

        // Отменяем бронирование
        ResponseEntity<TicketReservationDoneDto> cancelResp = restTemplate.exchange(
                "/api/ticketReservations/{id}/cancel",
                HttpMethod.PUT,
                null,
                TicketReservationDoneDto.class,
                reservationId
        );

        assertThat(cancelResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelResp.getBody()).isNotNull();
        assertThat(cancelResp.getBody().bookingStatus()).isEqualTo(BookingStatus.CANCELED);

        // GET /api/ticketReservations (список)
        ResponseEntity<TicketReservationDto[]> listResp = restTemplate.getForEntity(
                "/api/ticketReservations",
                TicketReservationDto[].class
        );
        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResp.getBody()).isNotNull();
        assertThat(listResp.getBody().length).isGreaterThanOrEqualTo(1);

        // POST /api/ticketReservations/cleanup/canceled-reservations
        ResponseEntity<CleanupResponse> cleanupResp = restTemplate.postForEntity(
                "/api/ticketReservations/cleanup/canceled-reservations",
                null,
                CleanupResponse.class
        );

        // Так как отмена свежая, скорее всего deletedCount будет 0 — это тоже валидный путь
        assertThat(cleanupResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cleanupResp.getBody()).isNotNull();
        assertThat(cleanupResp.getBody().deletedCount()).isGreaterThanOrEqualTo(0);
        assertThat(cleanupResp.getBody().message()).isNotBlank();
    }

    // ==========================
    // Вспомогательные методы
    // ==========================

    private ClientDoneDto createClient(String fullName,
                                       String phone,
                                       String email,
                                       String series,
                                       String number) {
        PassportCreateDto passport = new PassportCreateDto(series, number);
        ClientCreateWithDependenciesDto req = new ClientCreateWithDependenciesDto(
                fullName,
                phone,
                email,
                passport
        );
        ResponseEntity<ClientDoneDto> resp = restTemplate.postForEntity(
                "/api/clients",
                req,
                ClientDoneDto.class
        );
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        return resp.getBody();
    }

    private EventDoneDto createEvent(String name,
                                     LocalDate date,
                                     Integer seats,
                                     BigDecimal price,
                                     String description) {
        EventCreateDto req = new EventCreateDto(
                name,
                date,
                seats,
                price,
                description
        );
        ResponseEntity<EventDoneDto> resp = restTemplate.postForEntity(
                "/api/events",
                req,
                EventDoneDto.class
        );
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        return resp.getBody();
    }

    public record GlobalError(String error, String message) {}
}
