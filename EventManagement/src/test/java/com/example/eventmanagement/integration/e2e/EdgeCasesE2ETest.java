//package com.example.eventmanagement.integration.e2e;
//
//import com.example.eventmanagement.dto.*;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
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
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Testcontainers
//@Transactional
//class EdgeCasesE2ETest {
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
//    void edgeCase_MaximumSeatsReservation_ShouldWork() throws Exception {
//        // Граничный случай: бронирование всех доступных мест
//
//        // 1. СОЗДАЕМ МЕРОПРИЯТИЕ С 1 МЕСТОМ
//        EventCreateDto event = new EventCreateDto(
//                "Эксклюзивное мероприятие",
//                LocalDate.now().plusDays(30),
//                1,
//                BigDecimal.valueOf(10000),
//                "Только 1 место"
//        );
//
//        String eventResponse = mockMvc.perform(post("/api/events")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(event)))
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//        EventDoneDto eventDto = objectMapper.readValue(eventResponse, EventDoneDto.class);
//        Long eventId = eventDto.id();
//
//        // 2. СОЗДАЕМ КЛИЕНТА
//        ClientCreateWithDependenciesDto client = new ClientCreateWithDependenciesDto(
//                "Эксклюзивный клиент",
//                "+79165556677",
//                "exclusive@client.com",
//                new PassportCreateDto("1111", "222222")
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
//        // 3. БРОНИРУЕМ ЕДИНСТВЕННОЕ МЕСТО
//        TicketReservationCreateDto reservation = new TicketReservationCreateDto(
//                clientDto.id(),
//                eventId,
//                1,
//                com.example.eventmanagement.enums.BookingStatus.CONFIRMED
//        );
//
//        mockMvc.perform(post("/api/ticketReservations")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(reservation)))
//                .andExpect(status().isCreated());
//
//        // 4. ВТОРОЙ КЛИЕНТ ПЫТАЕТСЯ ЗАБРОНИРОВАТЬ - ДОЛЖНА БЫТЬ ОШИБКА
//        ClientCreateWithDependenciesDto client2 = new ClientCreateWithDependenciesDto(
//                "Второй клиент",
//                "+79166667788",
//                "second@client.com",
//                new PassportCreateDto("3333", "444444")
//        );
//
//        String client2Response = mockMvc.perform(post("/api/clients")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(client2)))
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//        ClientDoneDto client2Dto = objectMapper.readValue(client2Response, ClientDoneDto.class);
//
//        TicketReservationCreateDto reservation2 = new TicketReservationCreateDto(
//                client2Dto.id(),
//                eventId,
//                1,
//                com.example.eventmanagement.enums.BookingStatus.PENDING_CONFIRMATION
//        );
//
//        mockMvc.perform(post("/api/ticketReservations")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(reservation2)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_ERROR"));
//    }
//
//    @Test
//    void edgeCase_ZeroPriceEvent_ShouldAllowReservations() throws Exception {
//        // Граничный случай: мероприятие с нулевой ценой
//
//        EventCreateDto freeEvent = new EventCreateDto(
//                "Бесплатный вебинар",
//                LocalDate.now().plusDays(7),
//                500,
//                BigDecimal.ZERO,
//                "Бесплатное онлайн-мероприятие"
//        );
//
//        String eventResponse = mockMvc.perform(post("/api/events")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(freeEvent)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.ticketPrice").value(0))
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        // Проверяем, что статистика показывает 0 выручку
//        EventDoneDto eventDto = objectMapper.readValue(eventResponse, EventDoneDto.class);
//
//        mockMvc.perform(get("/api/events/{id}/statistics", eventDto.id()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.ticketPrice").value(0))
//                .andExpect(jsonPath("$.totalRevenue").value(0));
//    }
//
//    @Test
//    void edgeCase_UpdateClientToExistingPhoneOrEmail_ShouldFail() throws Exception {
//        // Граничный случай: обновление клиента на уже существующие данные
//
//        // 1. СОЗДАЕМ ДВУХ КЛИЕНТОВ
//        ClientCreateWithDependenciesDto client1 = new ClientCreateWithDependenciesDto(
//                "Клиент 1",
//                "+79161112233",
//                "client1@example.com",
//                new PassportCreateDto("1111", "222222")
//        );
//
//        String client1Response = mockMvc.perform(post("/api/clients")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(client1)))
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//        ClientDoneDto client1Dto = objectMapper.readValue(client1Response, ClientDoneDto.class);
//
//        ClientCreateWithDependenciesDto client2 = new ClientCreateWithDependenciesDto(
//                "Клиент 2",
//                "+79162223344",
//                "client2@example.com",
//                new PassportCreateDto("3333", "444444")
//        );
//
//        mockMvc.perform(post("/api/clients")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(client2)))
//                .andExpect(status().isCreated());
//
//        // 2. ПЫТАЕМСЯ ОБНОВИТЬ ПЕРВОГО КЛИЕНТА НА ДАННЫЕ ВТОРОГО
//        ClientCreateDto updateRequest = new ClientCreateDto(
//                "Клиент 1 Обновленный",
//                "+79162223344", // Телефон второго клиента
//                "client2@example.com" // Email второго клиента
//        );
//
//        mockMvc.perform(put("/api/clients/{id}", client1Dto.id())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isConflict());
//    }
//
//    @Test
//    void edgeCase_ReservationAfterEventStarted_ShouldFail() throws Exception {
//        // Тестируем, что нельзя создать бронирование после начала мероприятия
//        // Создаем мероприятие, которое уже началось (статус "проходит")
//
//        // 1. СОЗДАЕМ МЕРОПРИЯТИЕ
//        EventCreateDto event = new EventCreateDto(
//                "Текущее мероприятие",
//                LocalDate.now().plusDays(1), // Завтра
//                100,
//                BigDecimal.valueOf(1000),
//                "Мероприятие скоро начнется"
//        );
//
//        String eventResponse = mockMvc.perform(post("/api/events")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(event)))
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//        EventDoneDto eventDto = objectMapper.readValue(eventResponse, EventDoneDto.class);
//        Long eventId = eventDto.id();
//
//        // 2. МЕНЯЕМ СТАТУС НА "ПРОХОДИТ"
//        mockMvc.perform(put("/api/events/{id}/status", eventId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("\"проходит\""))
//                .andExpect(status().isOk());
//
//        // 3. СОЗДАЕМ КЛИЕНТА
//        ClientCreateWithDependenciesDto client = new ClientCreateWithDependenciesDto(
//                "Опоздавший клиент",
//                "+79167778899",
//                "late@client.com",
//                new PassportCreateDto("5555", "666666")
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
//        // 4. ПЫТАЕМСЯ СОЗДАТЬ БРОНИРОВАНИЕ - ДОЛЖНА БЫТЬ ОШИБКА
//        TicketReservationCreateDto reservation = new TicketReservationCreateDto(
//                clientDto.id(),
//                eventId,
//                2,
//                com.example.eventmanagement.enums.BookingStatus.PENDING_CONFIRMATION
//        );
//
//        mockMvc.perform(post("/api/ticketReservations")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(reservation)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_ERROR"));
//    }
//}