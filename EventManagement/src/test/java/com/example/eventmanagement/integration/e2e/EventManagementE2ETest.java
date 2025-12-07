package com.example.eventmanagement.integration.e2e;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.enums.EventStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@ActiveProfiles("testcontainers")
class EventManagementE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("e2e_test_db")
            .withUsername("e2e_user")
            .withPassword("e2e_password")
            .withReuse(false);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long createdClientId;
    private Long createdEventId;
    private Long createdReservationId;

    @BeforeEach
    void setUp() {
        // Каждый тест начинается с чистого состояния
        createdClientId = null;
        createdEventId = null;
        createdReservationId = null;
    }

    @Test
    void fullClientLifecycle_ShouldWorkEndToEnd() throws Exception {
        // 1. СОЗДАНИЕ КЛИЕНТА
        ClientCreateWithDependenciesDto clientRequest = new ClientCreateWithDependenciesDto(
                "Алексей Петров",
                "+79161234567",
                "alexey.petrov@example.com",
                new PassportCreateDto("4501", "123456")
        );

        String clientJson = objectMapper.writeValueAsString(clientRequest);

        String clientResponse = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fullName").value("Алексей Петров"))
                .andExpect(jsonPath("$.email").value("alexey.petrov@example.com"))
                .andExpect(jsonPath("$.passport.series").value("4501"))
                .andExpect(jsonPath("$.passport.number").value("123456"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ClientDoneDto clientDto = objectMapper.readValue(clientResponse, ClientDoneDto.class);
        createdClientId = clientDto.id();

        // 2. ПОЛУЧЕНИЕ СОЗДАННОГО КЛИЕНТА
        mockMvc.perform(get("/api/clients/{id}", createdClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdClientId))
                .andExpect(jsonPath("$.fullName").value("Алексей Петров"));

        // 3. ПОИСК КЛИЕНТА
        mockMvc.perform(get("/api/clients/search")
                        .param("searchTerm", "Алексей"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(createdClientId))
                .andExpect(jsonPath("$[0].fullName").value("Алексей Петров"));

        // 4. ОБНОВЛЕНИЕ КЛИЕНТА
        ClientCreateDto updateRequest = new ClientCreateDto(
                "Алексей Петров (обновленный)",
                "+79161234599",
                "alexey.new@example.com"
        );

        String updateJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/clients/{id}", createdClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Алексей Петров (обновленный)"))
                .andExpect(jsonPath("$.phoneNumber").value("+79161234599"));

        // 5. ОБНОВЛЕНИЕ ПАСПОРТА КЛИЕНТА
        PassportCreateDto newPassport = new PassportCreateDto("4502", "654321");

        mockMvc.perform(put("/api/clients/{id}/passport", createdClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPassport)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passport.series").value("4502"))
                .andExpect(jsonPath("$.passport.number").value("654321"));

        // 6. УДАЛЕНИЕ КЛИЕНТА (если нет активных бронирований)
        mockMvc.perform(delete("/api/clients/{id}", createdClientId))
                .andExpect(status().isNoContent());

        // 7. ПРОВЕРКА, ЧТО КЛИЕНТ УДАЛЕН
        mockMvc.perform(get("/api/clients/{id}", createdClientId))
                .andExpect(status().isNotFound());
    }

    @Test
    void fullEventLifecycle_ShouldWorkEndToEnd() throws Exception {
        // 1. СОЗДАНИЕ МЕРОПРИЯТИЯ
        EventCreateDto eventRequest = new EventCreateDto(
                "Тестовый концерт E2E",
                LocalDate.now().plusDays(30),
                150,
                BigDecimal.valueOf(2500.50),
                "Тестовое описание концерта"
        );

        String eventResponse = mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Тестовый концерт E2E"))
                .andExpect(jsonPath("$.numberOfSeats").value(150))
                .andExpect(jsonPath("$.ticketPrice").value(2500.50))
                .andExpect(jsonPath("$.status").value("запланировано"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EventDoneDto eventDto = objectMapper.readValue(eventResponse, EventDoneDto.class);
        createdEventId = eventDto.id();

        // 2. ПОЛУЧЕНИЕ СОЗДАННОГО МЕРОПРИЯТИЯ
        mockMvc.perform(get("/api/events/{id}", createdEventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdEventId))
                .andExpect(jsonPath("$.name").value("Тестовый концерт E2E"));

        // 3. ОБНОВЛЕНИЕ СТАТУСА МЕРОПРИЯТИЯ
        mockMvc.perform(put("/api/events/{id}/status", createdEventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"проходит\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("проходит"));

        // 4. ПОЛУЧЕНИЕ СТАТИСТИКИ (пока пустой, так как нет бронирований)
        mockMvc.perform(get("/api/events/{id}/statistics", createdEventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedTickets").value(0))
                .andExpect(jsonPath("$.totalRevenue").value(0));

        // 5. ВОЗВРАЩАЕМ СТАТУС НА "запланировано" для дальнейшего тестирования
        mockMvc.perform(put("/api/events/{id}/status", createdEventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"запланировано\""))
                .andExpect(status().isOk());

        // 6. УДАЛЕНИЕ МЕРОПРИЯТИЯ (нельзя удалить, так как статус не завершен/отменен)
        mockMvc.perform(delete("/api/events/{id}", createdEventId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_ERROR"));

        // 7. ОТМЕНА МЕРОПРИЯТИЯ
        mockMvc.perform(put("/api/events/{id}/status", createdEventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"отменено\""))
                .andExpect(status().isOk());

        // 8. ТЕПЕРЬ МОЖНО УДАЛИТЬ ОТМЕНЕННОЕ МЕРОПРИЯТИЕ
        mockMvc.perform(delete("/api/events/{id}", createdEventId))
                .andExpect(status().isNoContent());
    }

    @Test
    void fullReservationWorkflow_ShouldWorkEndToEnd() throws Exception {
        // 1. СОЗДАЕМ КЛИЕНТА
        ClientCreateWithDependenciesDto clientRequest = new ClientCreateWithDependenciesDto(
                "Мария Сидорова",
                "+79167778899",
                "maria.sidorova@example.com",
                new PassportCreateDto("4503", "112233")
        );

        String clientResponse = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        ClientDoneDto clientDto = objectMapper.readValue(clientResponse, ClientDoneDto.class);
        createdClientId = clientDto.id();

        // 2. СОЗДАЕМ МЕРОПРИЯТИЕ
        EventCreateDto eventRequest = new EventCreateDto(
                "Фестиваль музыки E2E",
                LocalDate.now().plusDays(45),
                50,
                BigDecimal.valueOf(3500),
                "Большой музыкальный фестиваль"
        );

        String eventResponse = mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        EventDoneDto eventDto = objectMapper.readValue(eventResponse, EventDoneDto.class);
        createdEventId = eventDto.id();

        // 3. СОЗДАЕМ БРОНИРОВАНИЕ
        TicketReservationCreateDto reservationRequest = new TicketReservationCreateDto(
                createdClientId,
                createdEventId,
                4,
                com.example.eventmanagement.enums.BookingStatus.PENDING_CONFIRMATION
        );

        String reservationResponse = mockMvc.perform(post("/api/ticketReservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.numberOfTickets").value(4))
                .andExpect(jsonPath("$.bookingStatus").value("ожидает подтверждения"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        TicketReservationDoneDto reservationDto = objectMapper.readValue(
                reservationResponse, TicketReservationDoneDto.class);
        createdReservationId = reservationDto.id();

        // 4. ПОДТВЕРЖДАЕМ БРОНИРОВАНИЕ
        mockMvc.perform(put("/api/ticketReservations/{id}/confirm", createdReservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("подтверждено"));

        // 5. ПРОВЕРЯЕМ СТАТИСТИКУ МЕРОПРИЯТИЯ
        mockMvc.perform(get("/api/events/{id}/statistics", createdEventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedTickets").value(4))
                .andExpect(jsonPath("$.totalRevenue").value(14000.00)); // 4 * 3500

        // 6. ОТМЕНЯЕМ БРОНИРОВАНИЕ
        mockMvc.perform(put("/api/ticketReservations/{id}/cancel", createdReservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("отменено"));

        // 7. ПРОВЕРЯЕМ, ЧТО СТАТИСТИКА ОБНОВИЛАСЬ
        mockMvc.perform(get("/api/events/{id}/statistics", createdEventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedTickets").value(0))
                .andExpect(jsonPath("$.totalRevenue").value(0));

        // 8. ОЧИСТКА СТАРЫХ ОТМЕНЕННЫХ БРОНИРОВАНИЙ
        // (тест проверяет, что эндпоинт работает, даже если нет данных для очистки)
        mockMvc.perform(post("/api/ticketReservations/cleanup/canceled-reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(0))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void complexBusinessScenario_ConcurrentReservations_ShouldWorkEndToEnd() throws Exception {
        // Сценарий: Несколько клиентов пытаются забронировать билеты на популярное мероприятие

        // 1. СОЗДАЕМ ПОПУЛЯРНОЕ МЕРОПРИЯТИЕ (всего 5 мест)
        EventCreateDto popularEvent = new EventCreateDto(
                "Популярный концерт",
                LocalDate.now().plusDays(10),
                5,
                BigDecimal.valueOf(5000),
                "Очень популярный концерт, билеты заканчиваются"
        );

        String eventResponse = mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(popularEvent)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        EventDoneDto eventDto = objectMapper.readValue(eventResponse, EventDoneDto.class);
        createdEventId = eventDto.id();

        // 2. СОЗДАЕМ 3-Х КЛИЕНТОВ
        Long[] clientIds = new Long[3];
        for (int i = 0; i < 3; i++) {
            ClientCreateWithDependenciesDto client = new ClientCreateWithDependenciesDto(
                    "Клиент " + (i + 1),
                    "+7916000000" + i,
                    "client" + (i + 1) + "@example.com",
                    new PassportCreateDto("100" + i, "20000" + i)
            );

            String clientResp = mockMvc.perform(post("/api/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(client)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            ClientDoneDto clientDto = objectMapper.readValue(clientResp, ClientDoneDto.class);
            clientIds[i] = clientDto.id();
        }

        // 3. КАЖДЫЙ КЛИЕНТ БРОНИРУЕТ ПО 2 БИЛЕТА
        Long[] reservationIds = new Long[3];
        for (int i = 0; i < 3; i++) {
            TicketReservationCreateDto reservation = new TicketReservationCreateDto(
                    clientIds[i],
                    createdEventId,
                    2,
                    com.example.eventmanagement.enums.BookingStatus.PENDING_CONFIRMATION
            );

            String reservationResp = mockMvc.perform(post("/api/ticketReservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reservation)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            TicketReservationDoneDto reservationDto = objectMapper.readValue(
                    reservationResp, TicketReservationDoneDto.class);
            reservationIds[i] = reservationDto.id();
        }

        // 4. ПОДТВЕРЖДАЕМ БРОНИРОВАНИЯ ДЛЯ ПЕРВЫХ ДВУХ КЛИЕНТОВ
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(put("/api/ticketReservations/{id}/confirm", reservationIds[i]))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookingStatus").value("подтверждено"));
        }

        // 5. ПЫТАЕМСЯ ПОДТВЕРДИТЬ ТРЕТЬЕ БРОНИРОВАНИЕ - ДОЛЖНА БЫТЬ ОШИБКА
        // Всего мест: 5, уже подтверждено: 2 + 2 = 4, осталось: 1 место
        // Но третье бронирование на 2 билета, поэтому должно быть отказано
        mockMvc.perform(put("/api/ticketReservations/{id}/confirm", reservationIds[2]))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_ERROR"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Недостаточно мест")));

        // 6. ПРОВЕРЯЕМ СТАТИСТИКУ: 4 подтвержденных билета, выручка 20000 (4 * 5000)
        mockMvc.perform(get("/api/events/{id}/statistics", createdEventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedTickets").value(4))
                .andExpect(jsonPath("$.totalRevenue").value(20000.00));

        // 7. ОТМЕНЯЕМ ОДНО ИЗ ПОДТВЕРЖДЕННЫХ БРОНИРОВАНИЙ
        mockMvc.perform(put("/api/ticketReservations/{id}/cancel", reservationIds[0]))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("отменено"));

        // 8. ТЕПЕРЬ МОЖЕМ ПОДТВЕРДИТЬ ТРЕТЬЕ БРОНИРОВАНИЕ
        // После отмены: подтверждено 2 билета, свободно 3 места, третье бронирование на 2 билета - проходит
        mockMvc.perform(put("/api/ticketReservations/{id}/confirm", reservationIds[2]))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("подтверждено"));

        // 9. ПРОВЕРЯЕМ ФИНАЛЬНУЮ СТАТИСТИКУ
        // Теперь подтверждено: 2 (второй клиент) + 2 (третий клиент) = 4 билета
        mockMvc.perform(get("/api/events/{id}/statistics", createdEventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedTickets").value(4))
                .andExpect(jsonPath("$.totalRevenue").value(20000.00));
    }

    @Test
    void validationAndErrorHandling_ShouldWorkCorrectly() throws Exception {
        // Тестируем различные ошибки валидации

        // 1. СОЗДАНИЕ КЛИЕНТА С НЕВАЛИДНЫМИ ДАННЫМИ
        // Неправильный телефон
        ClientCreateWithDependenciesDto invalidClient = new ClientCreateWithDependenciesDto(
                "К",
                "89161234567", // должен начинаться с +7
                "invalid-email",
                new PassportCreateDto("12", "123") // серия 4 цифры, номер 6 цифр
        );

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidClient)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message",
                        org.hamcrest.Matchers.containsString("fullName")))
                .andExpect(jsonPath("$.message",
                        org.hamcrest.Matchers.containsString("phoneNumber")))
                .andExpect(jsonPath("$.message",
                        org.hamcrest.Matchers.containsString("email")))
                .andExpect(jsonPath("$.message",
                        org.hamcrest.Matchers.containsString("series")))
                .andExpect(jsonPath("$.message",
                        org.hamcrest.Matchers.containsString("number")));

        // 2. СОЗДАНИЕ МЕРОПРИЯТИЯ С НЕВАЛИДНЫМИ ДАННЫМИ
        String invalidEventJson = """
        {
            "name": "",
            "date": "2020-01-01",
            "numberOfSeats": -1,
            "ticketPrice": -100,
            "description": ""
        }
        """;

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEventJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        // 3. ПОЛУЧЕНИЕ НЕСУЩЕСТВУЮЩЕГО КЛИЕНТА
        mockMvc.perform(get("/api/clients/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        // 4. ОБНОВЛЕНИЕ С НЕСУЩЕСТВУЮЩИМ ID
        ClientCreateDto updateDto = new ClientCreateDto(
                "Тест",
                "+79161234567",
                "test@example.com"
        );

        mockMvc.perform(put("/api/clients/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        // 5. ОБНОВЛЕНИЕ СТАТУСА С НЕВАЛИДНЫМ ЗНАЧЕНИЕМ
        // Сначала создаем реальное мероприятие
        EventCreateDto event = new EventCreateDto(
                "Тестовое мероприятие",
                LocalDate.now().plusDays(30),
                100,
                BigDecimal.valueOf(1000),
                "Описание"
        );

        String eventResp = mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        EventDoneDto eventDto = objectMapper.readValue(eventResp, EventDoneDto.class);
        Long eventId = eventDto.id();

        // Пытаемся обновить на несуществующий статус
        mockMvc.perform(put("/api/events/{id}/status", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"несуществующий статус\""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateConstraints_ShouldBeEnforced() throws Exception {
        // Тестируем уникальные ограничения

        // 1. СОЗДАЕМ ПЕРВОГО КЛИЕНТА
        ClientCreateWithDependenciesDto client1 = new ClientCreateWithDependenciesDto(
                "Иван Иванов",
                "+79161234567",
                "ivan@example.com",
                new PassportCreateDto("4501", "123456")
        );

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(client1)))
                .andExpect(status().isCreated());

        // 2. ПЫТАЕМСЯ СОЗДАТЬ КЛИЕНТА С ТЕМ ЖЕ EMAIL
        ClientCreateWithDependenciesDto client2 = new ClientCreateWithDependenciesDto(
                "Петр Петров",
                "+79161234568",
                "ivan@example.com", // Дублирующий email
                new PassportCreateDto("4502", "654321")
        );

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(client2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_ENTITY"));

        // 3. ПЫТАЕМСЯ СОЗДАТЬ КЛИЕНТА С ТЕМ ЖЕ ТЕЛЕФОНОМ
        ClientCreateWithDependenciesDto client3 = new ClientCreateWithDependenciesDto(
                "Сергей Сергеев",
                "+79161234567", // Дублирующий телефон
                "sergey@example.com",
                new PassportCreateDto("4503", "789012")
        );

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(client3)))
                .andExpect(status().isConflict());

        // 4. ПЫТАЕМСЯ СОЗДАТЬ КЛИЕНТА С ТЕМ ЖЕ ПАСПОРТОМ
        ClientCreateWithDependenciesDto client4 = new ClientCreateWithDependenciesDto(
                "Анна Аннова",
                "+79161234569",
                "anna@example.com",
                new PassportCreateDto("4501", "123456") // Дублирующий паспорт
        );

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(client4)))
                .andExpect(status().isConflict());

        // 5. СОЗДАЕМ МЕРОПРИЯТИЕ
        EventCreateDto event1 = new EventCreateDto(
                "Концерт",
                LocalDate.now().plusDays(30),
                100,
                BigDecimal.valueOf(1000),
                "Описание"
        );

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event1)))
                .andExpect(status().isCreated());

        // 6. ПЫТАЕМСЯ СОЗДАТЬ МЕРОПРИЯТИЕ С ТЕМ ЖЕ ИМЕНЕМ И ДАТОЙ
        EventCreateDto event2 = new EventCreateDto(
                "Концерт", // То же имя
                LocalDate.now().plusDays(30), // Та же дата
                50,
                BigDecimal.valueOf(500),
                "Другое описание"
        );

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event2)))
                .andExpect(status().isConflict());
    }

    @Test
    void getAllEndpoints_ShouldReturnCorrectData() throws Exception {
        // Подготовка данных
        // Создаем 3 клиентов
        for (int i = 1; i <= 3; i++) {
            ClientCreateWithDependenciesDto client = new ClientCreateWithDependenciesDto(
                    "Клиент " + i,
                    "+7916000000" + i,
                    "client" + i + "@example.com",
                    new PassportCreateDto("100" + i, "20000" + i)
            );
            mockMvc.perform(post("/api/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(client)))
                    .andExpect(status().isCreated());
        }

        // Создаем 2 мероприятия
        for (int i = 1; i <= 2; i++) {
            EventCreateDto event = new EventCreateDto(
                    "Мероприятие " + i,
                    LocalDate.now().plusDays(10 * i),
                    50 + i * 10,
                    BigDecimal.valueOf(1000 * i),
                    "Описание " + i
            );
            mockMvc.perform(post("/api/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(event)))
                    .andExpect(status().isCreated());
        }

        // 1. ПОЛУЧАЕМ ВСЕХ КЛИЕНТОВ
        String clientsResponse = mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<Map<String, Object>> clients = objectMapper.readValue(
                clientsResponse, List.class);
        assertThat(clients).hasSize(3);

        // 2. ПОЛУЧАЕМ ВСЕ МЕРОПРИЯТИЯ
        String eventsResponse = mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<Map<String, Object>> events = objectMapper.readValue(
                eventsResponse, List.class);
        assertThat(events).hasSize(2);

        // 3. ПРОВЕРЯЕМ, ЧТО ПОИСК РАБОТАЕТ
        mockMvc.perform(get("/api/clients/search")
                        .param("searchTerm", "Клиент"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(3)));

        mockMvc.perform(get("/api/clients/search")
                        .param("searchTerm", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void dateBoundaryConditions_ShouldBeHandledCorrectly() throws Exception {
        // Тестируем граничные условия с датами

        // 1. СОЗДАЕМ КЛИЕНТА И МЕРОПРИЯТИЕ НА ЗАВТРА
        ClientCreateWithDependenciesDto client = new ClientCreateWithDependenciesDto(
                "Граничный Тест",
                "+79161112233",
                "boundary@test.com",
                new PassportCreateDto("9999", "888888")
        );

        String clientResp = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(client)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        ClientDoneDto clientDto = objectMapper.readValue(clientResp, ClientDoneDto.class);
        Long clientId = clientDto.id();

        // Мероприятие на завтра (граничный случай для отмены)
        EventCreateDto tomorrowEvent = new EventCreateDto(
                "Завтрашний концерт",
                LocalDate.now().plusDays(1),
                10,
                BigDecimal.valueOf(1000),
                "Мероприятие на завтра"
        );

        String eventResp = mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tomorrowEvent)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        EventDoneDto eventDto = objectMapper.readValue(eventResp, EventDoneDto.class);
        Long eventId = eventDto.id();

        // 2. СОЗДАЕМ БРОНИРОВАНИЕ
        TicketReservationCreateDto reservation = new TicketReservationCreateDto(
                clientId,
                eventId,
                2,
                com.example.eventmanagement.enums.BookingStatus.PENDING_CONFIRMATION
        );

        String reservationResp = mockMvc.perform(post("/api/ticketReservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        TicketReservationDoneDto reservationDto = objectMapper.readValue(
                reservationResp, TicketReservationDoneDto.class);
        Long reservationId = reservationDto.id();

        // 3. ОТМЕНА БРОНИРОВАНИЯ ЗА ДЕНЬ ДО МЕРОПРИЯТИЯ - ДОЛЖНА ПРОЙТИ
        mockMvc.perform(put("/api/ticketReservations/{id}/cancel", reservationId))
                .andExpect(status().isOk());

        // 4. СОЗДАЕМ МЕРОПРИЯТИЕ С ПРОШЕДШЕЙ ДАТОЙ - ДОЛЖНА БЫТЬ ОШИБКА
        EventCreateDto pastEvent = new EventCreateDto(
                "Прошедший концерт",
                LocalDate.now().minusDays(1),
                10,
                BigDecimal.valueOf(1000),
                "Мероприятие в прошлом"
        );

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pastEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_ERROR"));

        // 5. СОЗДАЕМ МЕРОПРИЯТИЕ С СЕГОДНЯШНЕЙ ДАТОЙ - ТАКЖЕ ОШИБКА
        EventCreateDto todayEvent = new EventCreateDto(
                "Сегодняшний концерт",
                LocalDate.now(),
                10,
                BigDecimal.valueOf(1000),
                "Мероприятие сегодня"
        );

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todayEvent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cascadeOperations_ShouldWorkCorrectly() throws Exception {
        // Тестируем каскадные операции

        // 1. СОЗДАЕМ КЛИЕНТА С ПАСПОРТОМ
        ClientCreateWithDependenciesDto client = new ClientCreateWithDependenciesDto(
                "Каскадный Тест",
                "+79162223344",
                "cascade@test.com",
                new PassportCreateDto("7777", "888888")
        );

        String clientResp = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(client)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        ClientDoneDto clientDto = objectMapper.readValue(clientResp, ClientDoneDto.class);
        Long clientId = clientDto.id();

        // 2. СОЗДАЕМ МЕРОПРИЯТИЕ
        EventCreateDto event = new EventCreateDto(
                "Каскадное мероприятие",
                LocalDate.now().plusDays(30),
                10,
                BigDecimal.valueOf(1000),
                "Тест каскадных операций"
        );

        String eventResp = mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        EventDoneDto eventDto = objectMapper.readValue(eventResp, EventDoneDto.class);
        Long eventId = eventDto.id();

        // 3. СОЗДАЕМ БРОНИРОВАНИЕ
        TicketReservationCreateDto reservation = new TicketReservationCreateDto(
                clientId,
                eventId,
                1,
                com.example.eventmanagement.enums.BookingStatus.CONFIRMED
        );

        String reservationResp = mockMvc.perform(post("/api/ticketReservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        TicketReservationDoneDto reservationDto = objectMapper.readValue(
                reservationResp, TicketReservationDoneDto.class);
        Long reservationId = reservationDto.id();

        // 4. ПЫТАЕМСЯ УДАЛИТЬ КЛИЕНТА - НЕ ДОЛЖНО ПОЛУЧИТЬСЯ (есть активное бронирование)
        mockMvc.perform(delete("/api/clients/{id}", clientId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_ERROR"));

        // 5. ОТМЕНЯЕМ БРОНИРОВАНИЕ
        mockMvc.perform(put("/api/ticketReservations/{id}/cancel", reservationId))
                .andExpect(status().isOk());

        // 6. ТЕПЕРЬ МОЖЕМ УДАЛИТЬ КЛИЕНТА - ПАСПОРТ ДОЛЖЕН УДАЛИТЬСЯ КАСКАДНО
        mockMvc.perform(delete("/api/clients/{id}", clientId))
                .andExpect(status().isNoContent());

        // 7. УДАЛЯЕМ МЕРОПРИЯТИЕ (сначала отменяем его)
        mockMvc.perform(put("/api/events/{id}/status", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"отменено\""))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/events/{id}", eventId))
                .andExpect(status().isNoContent());

        // 8. ПРОВЕРЯЕМ, ЧТО БРОНИРОВАНИЕ УДАЛИЛОСЬ КАСКАДНО ПРИ УДАЛЕНИИ КЛИЕНТА И МЕРОПРИЯТИЯ
        mockMvc.perform(get("/api/ticketReservations/{id}", reservationId))
                .andExpect(status().isNotFound());
    }
}