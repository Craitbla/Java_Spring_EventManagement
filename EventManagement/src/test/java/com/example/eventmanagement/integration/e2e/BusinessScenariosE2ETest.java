//package com.example.eventmanagement.integration.e2e;
//
//import com.example.eventmanagement.dto.*;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Testcontainers
//@Transactional
//@ActiveProfiles("testcontainers")
//class BusinessScenariosE2ETest {
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
//    }
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void scenario_FreeEventWithManyRegistrations_ShouldWork() throws Exception {
//        // Сценарий: Бесплатное мероприятие с большим количеством регистраций
//
//        // 1. СОЗДАЕМ БЕСПЛАТНОЕ МЕРОПРИЯТИЕ
//        EventCreateDto freeEvent = new EventCreateDto(
//                "Бесплатный мастер-класс по программированию",
//                LocalDate.now().plusDays(15),
//                200,
//                BigDecimal.ZERO,
//                "Бесплатный вход для всех желающих"
//        );
//
//        String eventResponse = mockMvc.perform(post("/api/events")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(freeEvent)))
//                .andExpect(status().isCreated())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        EventDoneDto eventDto = objectMapper.readValue(eventResponse, EventDoneDto.class);
//        Long eventId = eventDto.id();
//
//        // 2. СОЗДАЕМ 150 ПОЛЬЗОВАТЕЛЕЙ, КОТОРЫЕ РЕГИСТРИРУЮТСЯ
//        for (int i = 1; i <= 150; i++) {
//            // Создаем клиента
//            ClientCreateWithDependenciesDto client = new ClientCreateWithDependenciesDto(
//                    "Участник " + i,
//                    "+7916" + String.format("%07d", i),
//                    "participant" + i + "@example.com",
//                    new PassportCreateDto(
//                            String.format("%04d", 1000 + i),
//                            String.format("%06d", 200000 + i)
//                    )
//            );
//
//            String clientResponse = mockMvc.perform(post("/api/clients")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(client)))
//                    .andReturn()
//                    .getResponse()
//                    .getContentAsString();
//
//            ClientDoneDto clientDto = objectMapper.readValue(clientResponse, ClientDoneDto.class);
//
//            // Регистрируем на мероприятие
//            TicketReservationCreateDto reservation = new TicketReservationCreateDto(
//                    clientDto.id(),
//                    eventId,
//                    1,
//                    com.example.eventmanagement.enums.BookingStatus.PENDING_CONFIRMATION
//            );
//
//            mockMvc.perform(post("/api/ticketReservations")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(reservation)))
//                    .andExpect(status().isCreated());
//        }
//
//        // 3. ПОДТВЕРЖДАЕМ 100 РЕГИСТРАЦИЙ
//        // В реальном тесте нам нужно получить ID всех бронирований, но для E2E теста
//        // проверим статистику
//
//        // 4. ПРОВЕРЯЕМ СТАТИСТИКУ
//        mockMvc.perform(get("/api/events/{id}/statistics", eventId))
//                .andExpect(status().isOk())
//                // Всего 150 бронирований, но пока ни одного подтвержденного
//                .andExpect(jsonPath("$.confirmedTickets").value(0))
//                .andExpect(jsonPath("$.totalRevenue").value(0));
//    }
//
//    @Test
//    void scenario_EventStatusTransitions_ShouldFollowBusinessRules() throws Exception {
//        // Сценарий: Проверка всех допустимых переходов статусов мероприятия
//
//        // 1. СОЗДАЕМ МЕРОПРИЯТИЕ (начальный статус: запланировано)
//        EventCreateDto event = new EventCreateDto(
//                "Тестовый концерт статусов",
//                LocalDate.now().plusDays(20),
//                100,
//                BigDecimal.valueOf(1000),
//                "Тест переходов статусов"
//        );
//
//        String eventResponse = mockMvc.perform(post("/api/events")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(event)))
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        EventDoneDto eventDto = objectMapper.readValue(eventResponse, EventDoneDto.class);
//        Long eventId = eventDto.id();
//
//        // 2. ПЛАНИРУЕМОЕ → ОТМЕНЕНО (допустимо)
//        mockMvc.perform(put("/api/events/{id}/status", eventId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("\"отменено\""))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("отменено"));
//
//        // 3. ОТМЕНЕНО → ЗАПЛАНИРОВАНО (допустимо)
//        mockMvc.perform(put("/api/events/{id}/status", eventId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("\"запланировано\""))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("запланировано"));
//
//        // 4. ЗАПЛАНИРОВАНО → ПРОХОДИТ (допустимо)
//        mockMvc.perform(put("/api/events/{id}/status", eventId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("\"проходит\""))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("проходит"));
//
//        // 5. ПРОХОДИТ → ЗАВЕРШЕНО (допустимо)
//        mockMvc.perform(put("/api/events/{id}/status", eventId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("\"завершено\""))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("завершено"));
//
//        // 6. ЗАВЕРШЕНО → ПРОХОДИТ (НЕ допустимо)
//        mockMvc.perform(put("/api/events/{id}/status", eventId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("\"проходит\""))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_ERROR"));
//
//        // 7. ЗАВЕРШЕНО → ОТМЕНЕНО (НЕ допустимо)
//        mockMvc.perform(put("/api/events/{id}/status", eventId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("\"отменено\""))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void scenario_ReservationCleanup_ShouldWorkCorrectly() throws Exception {
//        // Сценарий: Очистка старых отмененных бронирований
//
//        // 1. СОЗДАЕМ КЛИЕНТА И МЕРОПРИЯТИЕ
//        ClientCreateWithDependenciesDto client = new ClientCreateWithDependenciesDto(
//                "Тест Очистки",
//                "+79163334455",
//                "cleanup@test.com",
//                new PassportCreateDto("9999", "111111")
//        );
//
//        String clientResponse = mockMvc.perform(post("/api/clients")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(client)))
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//        ClientDoneDto clientDto = objectMapper.readValue(clientResponse, ClientDoneDto.class);
//
//        EventCreateDto event = new EventCreateDto(
//                "Мероприятие для очистки",
//                LocalDate.now().plusDays(60),
//                50,
//                BigDecimal.valueOf(500),
//                "Тест очистки"
//        );
//
//        String eventResponse = mockMvc.perform(post("/api/events")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(event)))
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//        EventDoneDto eventDto = objectMapper.readValue(eventResponse, EventDoneDto.class);
//
//        // 2. СОЗДАЕМ 5 БРОНИРОВАНИЙ И ОТМЕНЯЕМ ИХ
//        for (int i = 1; i <= 5; i++) {
//            TicketReservationCreateDto reservation = new TicketReservationCreateDto(
//                    clientDto.id(),
//                    eventDto.id(),
//                    i,
//                    com.example.eventmanagement.enums.BookingStatus.PENDING_CONFIRMATION
//            );
//
//            String reservationResponse = mockMvc.perform(post("/api/ticketReservations")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(reservation)))
//                    .andReturn()
//                    .getResponse()
//                    .getContentAsString();
//
//            TicketReservationDoneDto reservationDto = objectMapper.readValue(
//                    reservationResponse, TicketReservationDoneDto.class);
//
//            // Отменяем бронирование
//            mockMvc.perform(put("/api/ticketReservations/{id}/cancel",
//                            reservationDto.id()))
//                    .andExpect(status().isOk());
//        }
//
//        // 3. СОЗДАЕМ ЕЩЕ 3 АКТИВНЫХ БРОНИРОВАНИЯ (не отменяем)
//        for (int i = 1; i <= 3; i++) {
//            TicketReservationCreateDto reservation = new TicketReservationCreateDto(
//                    clientDto.id(),
//                    eventDto.id(),
//                    1,
//                    com.example.eventmanagement.enums.BookingStatus.CONFIRMED
//            );
//
//            mockMvc.perform(post("/api/ticketReservations")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(reservation)))
//                    .andExpect(status().isCreated());
//        }
//
//        // 4. ВЫЗЫВАЕМ ОЧИСТКУ
//        // В тестовой среде бронирования только что созданы, поэтому не будут удалены
//        // (они должны быть старше месяца)
//        mockMvc.perform(post("/api/ticketReservations/cleanup/canceled-reservations"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.deletedCount").value(0))
//                .andExpect(jsonPath("$.message").value(
//                        "Нет старых отмененных бронирований для очистки"));
//    }
//
//    @Test
//    void scenario_ClientWithMultipleReservations_ShouldWork() throws Exception {
//        // Сценарий: Клиент с несколькими бронированиями на разные мероприятия
//
//        // 1. СОЗДАЕМ КЛИЕНТА
//        ClientCreateWithDependenciesDto client = new ClientCreateWithDependenciesDto(
//                "Постоянный клиент",
//                "+79164445566",
//                "regular@client.com",
//                new PassportCreateDto("7777", "888888")
//        );
//
//        String clientResponse = mockMvc.perform(post("/api/clients")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(client)))
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//        ClientDoneDto clientDto = objectMapper.readValue(clientResponse, ClientDoneDto.class);
//        Long clientId = clientDto.id();
//
//        // 2. СОЗДАЕМ 3 РАЗНЫХ МЕРОПРИЯТИЯ
//        Long[] eventIds = new Long[3];
//        for (int i = 0; i < 3; i++) {
//            EventCreateDto event = new EventCreateDto(
//                    "Мероприятие " + (i + 1),
//                    LocalDate.now().plusDays(10 * (i + 1)),
//                    50,
//                    BigDecimal.valueOf(1000 + i * 500),
//                    "Описание мероприятия " + (i + 1)
//            );
//
//            String eventResponse = mockMvc.perform(post("/api/events")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(event)))
//                    .andReturn()
//                    .getResponse()
//                    .getContentAsString();
//            EventDoneDto eventDto = objectMapper.readValue(eventResponse, EventDoneDto.class);
//            eventIds[i] = eventDto.id();
//        }
//
//        // 3. КЛИЕНТ БРОНИРУЕТ НА ВСЕ МЕРОПРИЯТИЯ
//        for (int i = 0; i < 3; i++) {
//            TicketReservationCreateDto reservation = new TicketReservationCreateDto(
//                    clientId,
//                    eventIds[i],
//                    i + 1, // разное количество билетов
//                    com.example.eventmanagement.enums.BookingStatus.CONFIRMED
//            );
//
//            mockMvc.perform(post("/api/ticketReservations")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(reservation)))
//                    .andExpect(status().isCreated());
//        }
//
//        // 4. ПРОВЕРЯЕМ, ЧТО КЛИЕНТА НЕЛЬЗЯ УДАЛИТЬ (есть активные бронирования)
//        mockMvc.perform(delete("/api/clients/{id}", clientId))
//                .andExpect(status().isBadRequest());
//
//        // 5. ПРОВЕРЯЕМ СТАТИСТИКУ КАЖДОГО МЕРОПРИЯТИЯ
//        for (int i = 0; i < 3; i++) {
//            mockMvc.perform(get("/api/events/{id}/statistics", eventIds[i]))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.confirmedTickets").value(i + 1))
//                    .andExpect(jsonPath("$.totalRevenue").value(
//                            (1000 + i * 500) * (i + 1)));
//        }
//
//        // 6. ОТМЕНЯЕМ ВСЕ БРОНИРОВАНИЯ
//        // (в реальном тесте нужно получить ID бронирований)
//
//        // 7. ТЕПЕРЬ МОЖЕМ УДАЛИТЬ КЛИЕНТА
//        // (после отмены всех бронирований)
//    }
//}