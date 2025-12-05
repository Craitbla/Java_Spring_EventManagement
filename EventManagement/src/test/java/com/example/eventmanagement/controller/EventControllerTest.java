package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.enums.EventStatus;
import com.example.eventmanagement.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService eventService;

    private EventDto eventDto1;
    private EventDto eventDto2;
    private EventDoneDto eventDoneDto;
    private EventCreateDto eventCreateDto;
    private EventStatisticsDto eventStatisticsDto;

    @BeforeEach
    void setUp() {
        eventDto1 = new EventDto(
                1L,
                "Концерт классической музыки",
                LocalDate.now().plusDays(30),
                100,
                new BigDecimal("1500.00"),
                EventStatus.PLANNED,
                "Концерт симфонического оркестра"
        );

        eventDto2 = new EventDto(
                2L,
                "Выставка современного искусства",
                LocalDate.now().plusDays(15),
                50,
                new BigDecimal("500.00"),
                EventStatus.ONGOING,
                "Выставка современных художников"
        );

        eventDoneDto = new EventDoneDto(
                1L,
                "Концерт классической музыки",
                LocalDate.now().plusDays(30),
                100,
                new BigDecimal("1500.00"),
                EventStatus.PLANNED,
                "Концерт симфонического оркестра",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        eventCreateDto = new EventCreateDto(
                "Новый концерт",
                LocalDate.now().plusDays(60),
                200,
                new BigDecimal("2000.00"),
                EventStatus.PLANNED,
                "Описание нового концерта"
        );

        eventStatisticsDto = new EventStatisticsDto(
                1L,
                "Концерт классической музыки",
                LocalDate.now().plusDays(30),
                100,
                EventStatus.PLANNED,
                85,
                new BigDecimal("1500.00"),
                new BigDecimal("127500.00")
        );
    }

    @Test
    void getAllEvents_ShouldReturnListOfEvents() throws Exception {
        List<EventDto> events = Arrays.asList(eventDto1, eventDto2);
        when(eventService.getAll()).thenReturn(events);

        mockMvc.perform(get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Концерт классической музыки")))
                .andExpect(jsonPath("$[0].ticketPrice", is(1500.00)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Выставка современного искусства")));

        verify(eventService, times(1)).getAll();
    }

    @Test
    void getEventById_WithValidId_ShouldReturnEvent() throws Exception {
        when(eventService.getById(1L)).thenReturn(eventDoneDto);

        mockMvc.perform(get("/api/events/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Концерт классической музыки")))
                .andExpect(jsonPath("$.ticketPrice", is(1500.00)))
                .andExpect(jsonPath("$.numberOfSeats", is(100)))
                .andExpect(jsonPath("$.status", is("PLANNED")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));

        verify(eventService, times(1)).getById(1L);
    }

    @Test
    void getEventById_WithNonExistentId_ShouldReturn404() throws Exception {
        when(eventService.getById(999L))
                .thenThrow(new com.example.eventmanagement.exception.EntityNotFoundException(
                        "Мероприятие с id 999 не найдено"));

        mockMvc.perform(get("/api/events/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("не найдено")));

        verify(eventService, times(1)).getById(999L);
    }

    @Test
    void getEventStatisticsById_WithValidId_ShouldReturnStatistics() throws Exception {
        when(eventService.getEventStatistics(1L)).thenReturn(eventStatisticsDto);

        mockMvc.perform(get("/api/events/{id}/statistics", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Концерт классической музыки")))
                .andExpect(jsonPath("$.confirmedTickets", is(85)))
                .andExpect(jsonPath("$.totalRevenue", is(127500.00)))
                .andExpect(jsonPath("$.ticketPrice", is(1500.00)));

        verify(eventService, times(1)).getEventStatistics(1L);
    }

    @Test
    void getEventStatisticsById_WithNonExistentId_ShouldReturn404() throws Exception {
        when(eventService.getEventStatistics(999L))
                .thenThrow(new com.example.eventmanagement.exception.EntityNotFoundException(
                        "Мероприятие с id 999 не найдено"));

        mockMvc.perform(get("/api/events/{id}/statistics", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));

        verify(eventService, times(1)).getEventStatistics(999L);
    }

    @Test
    void createEvent_WithValidData_ShouldReturnCreatedEvent() throws Exception {
        EventDoneDto createdEvent = new EventDoneDto(
                3L,
                "Новый концерт",
                LocalDate.now().plusDays(60),
                200,
                new BigDecimal("2000.00"),
                EventStatus.PLANNED,
                "Описание нового концерта",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(eventService.createEvent(any(EventCreateDto.class)))
                .thenReturn(createdEvent);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("Новый концерт")))
                .andExpect(jsonPath("$.numberOfSeats", is(200)))
                .andExpect(jsonPath("$.ticketPrice", is(2000.00)))
                .andExpect(jsonPath("$.status", is("PLANNED")));

        verify(eventService, times(1)).createEvent(any(EventCreateDto.class));
    }

    @Test
    void createEvent_WithInvalidData_ShouldReturn400() throws Exception {
        EventCreateDto invalidDto = new EventCreateDto(
                "",
                LocalDate.now().minusDays(1),
                -1,
                new BigDecimal("-100.00"),
                EventStatus.PLANNED,
                ""
        );

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("name")))
                .andExpect(jsonPath("$.message", containsString("date")))
                .andExpect(jsonPath("$.message", containsString("numberOfSeats")))
                .andExpect(jsonPath("$.message", containsString("ticketPrice")));

        verify(eventService, never()).createEvent(any());
    }

    @Test
    void createEvent_WithDuplicateEvent_ShouldReturn409() throws Exception {
        when(eventService.createEvent(any(EventCreateDto.class)))
                .thenThrow(new com.example.eventmanagement.exception.DuplicateEntityException(
                        "Такое мероприятие уже существует: Новый концерт " + LocalDate.now().plusDays(60)));

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("DUPLICATE_ENTITY")))
                .andExpect(jsonPath("$.message", containsString("уже существует")));

        verify(eventService, times(1)).createEvent(any(EventCreateDto.class));
    }

    @Test
    void createEvent_WithPastDate_ShouldReturn400() throws Exception {
        when(eventService.createEvent(any(EventCreateDto.class)))
                .thenThrow(new com.example.eventmanagement.exception.BusinessValidationException(
                        "Нельзя создать мероприятие с прошедшей датой"));

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BUSINESS_RULE_ERROR")))
                .andExpect(jsonPath("$.message", containsString("прошедшей датой")));

        verify(eventService, times(1)).createEvent(any(EventCreateDto.class));
    }

    @Test
    void updateEventStatus_WithValidData_ShouldReturnUpdatedEvent() throws Exception {
        EventDoneDto updatedEvent = new EventDoneDto(
                1L,
                "Концерт классической музыки",
                LocalDate.now().plusDays(30),
                100,
                new BigDecimal("1500.00"),
                EventStatus.ONGOING,
                "Концерт симфонического оркестра",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        when(eventService.updateEventStatus(eq(1L), eq(EventStatus.ONGOING)))
                .thenReturn(updatedEvent);

        mockMvc.perform(put("/api/events/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"проходит\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("проходит")));

        verify(eventService, times(1)).updateEventStatus(eq(1L), eq(EventStatus.ONGOING));
    }

    @Test
    void updateEventStatus_WithInvalidStatusTransition_ShouldReturn400() throws Exception {
        when(eventService.updateEventStatus(eq(1L), eq(EventStatus.CANCELED)))
                .thenThrow(new com.example.eventmanagement.exception.BusinessValidationException(
                        "Статус запланировано не может поменяться на отменено"));

        mockMvc.perform(put("/api/events/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"отменено\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BUSINESS_RULE_ERROR")))
                .andExpect(jsonPath("$.message", containsString("не может поменяться")));

        verify(eventService, times(1)).updateEventStatus(eq(1L), eq(EventStatus.CANCELED));
    }

    @Test
    void updateEventStatus_WithNonExistentId_ShouldReturn404() throws Exception {
        when(eventService.updateEventStatus(eq(999L), any(EventStatus.class)))
                .thenThrow(new com.example.eventmanagement.exception.EntityNotFoundException(
                        "Мероприятие по id 999 не найдено"));

        mockMvc.perform(put("/api/events/{id}/status", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"запланировано\""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));

        verify(eventService, times(1)).updateEventStatus(eq(999L), any(EventStatus.class));
    }

    @Test
    void updateEventStatus_WithInvalidStatusValue_ShouldReturn400() throws Exception {
        mockMvc.perform(put("/api/events/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"неизвестный статус\""))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).updateEventStatus(any(), any());
    }

    @Test
    void deleteEvent_WithValidId_ShouldReturn204() throws Exception {
        doNothing().when(eventService).deleteEvent(1L);

        mockMvc.perform(delete("/api/events/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(eventService, times(1)).deleteEvent(1L);
    }

    @Test
    void deleteEvent_WithNonExistentId_ShouldReturn404() throws Exception {
        doThrow(new com.example.eventmanagement.exception.EntityNotFoundException(
                "Мероприятие с id 999 не найдено"))
                .when(eventService).deleteEvent(999L);

        mockMvc.perform(delete("/api/events/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(eventService, times(1)).deleteEvent(999L);
    }

    @Test
    void deleteEvent_WithActiveEvent_ShouldReturn400() throws Exception {
        doThrow(new com.example.eventmanagement.exception.BusinessValidationException(
                "Это мероприятие с id 1 нельзя удалить, оно еще проходит или будет проходить"))
                .when(eventService).deleteEvent(1L);

        mockMvc.perform(delete("/api/events/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BUSINESS_RULE_ERROR")))
                .andExpect(jsonPath("$.message", containsString("нельзя удалить")));

        verify(eventService, times(1)).deleteEvent(1L);
    }

    @Test
    void getAllEvents_WithEmptyDatabase_ShouldReturnEmptyList() throws Exception {
        when(eventService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(eventService, times(1)).getAll();
    }

    @Test
    void createEvent_WithMissingRequiredFields_ShouldReturn400() throws Exception {
        String invalidJson = """
                {
                    "date": "2024-12-31",
                    "numberOfSeats": 100,
                    "ticketPrice": 1500.00
                }
                """;

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).createEvent(any());
    }

    @Test
    void updateEventStatus_WithEmptyBody_ShouldReturn400() throws Exception {
        mockMvc.perform(put("/api/events/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).updateEventStatus(any(), any());
    }

    @Test
    void createEvent_WithZeroSeats_ShouldReturn400() throws Exception {
        EventCreateDto invalidDto = new EventCreateDto(
                "Концерт",
                LocalDate.now().plusDays(30),
                0,
                new BigDecimal("1000.00"),
                EventStatus.PLANNED,
                "Описание"
        );

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("numberOfSeats")));

        verify(eventService, never()).createEvent(any());
    }

    @Test
    void createEvent_WithNullTicketPrice_ShouldReturn400() throws Exception {
        String invalidJson = """
                {
                    "name": "Концерт",
                    "date": "2024-12-31",
                    "numberOfSeats": 100,
                    "status": "запланировано"
                }
                """;

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(eventService, never()).createEvent(any());
    }
}
