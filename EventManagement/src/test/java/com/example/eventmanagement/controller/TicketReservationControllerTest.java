package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.service.TicketReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketReservationController.class)
class TicketReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketReservationService ticketReservationService;

    private TicketReservationDto reservationDto1;
    private TicketReservationDto reservationDto2;
    private TicketReservationDoneDto reservationDoneDto;
    private TicketReservationCreateDto reservationCreateDto;
    private ClientCreateDto clientDto;
    private EventCreateDto eventDto;

    @BeforeEach
    void setUp() {
        clientDto = new ClientCreateDto(
                "Иван Иванов",
                "+79123456789",
                "ivan@mail.ru"
        );

        eventDto = new EventCreateDto(
                "Концерт классической музыки",
                java.time.LocalDate.now().plusDays(30),
                100,
                java.math.BigDecimal.valueOf(1500.00),
                "Концерт симфонического оркестра"
        );

        reservationDto1 = new TicketReservationDto(
                1L,
                clientDto,
                eventDto,
                2,
                BookingStatus.PENDING_CONFIRMATION
        );

        reservationDto2 = new TicketReservationDto(
                2L,
                clientDto,
                eventDto,
                1,
                BookingStatus.CONFIRMED
        );

        reservationDoneDto = new TicketReservationDoneDto(
                1L,
                clientDto,
                eventDto,
                2,
                BookingStatus.PENDING_CONFIRMATION,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        reservationCreateDto = new TicketReservationCreateDto(
                1L,
                1L,
                2,
                BookingStatus.PENDING_CONFIRMATION
        );
    }

    @Test
    void getAllTicketReservations_ShouldReturnListOfReservations() throws Exception {
        List<TicketReservationDto> reservations = Arrays.asList(reservationDto1, reservationDto2);
        when(ticketReservationService.getAll()).thenReturn(reservations);

        mockMvc.perform(get("/api/ticketReservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].numberOfTickets", is(2)))
                .andExpect(jsonPath("$[0].bookingStatus", is("ожидает подтверждения")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].numberOfTickets", is(1)))
                .andExpect(jsonPath("$[1].bookingStatus", is("подтверждено")));

        verify(ticketReservationService, times(1)).getAll();
    }

    @Test
    void getTicketReservationById_WithValidId_ShouldReturnReservation() throws Exception {
        when(ticketReservationService.getById(1L)).thenReturn(reservationDoneDto);

        mockMvc.perform(get("/api/ticketReservations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.numberOfTickets", is(2)))
                .andExpect(jsonPath("$.bookingStatus", is("ожидает подтверждения")))
                .andExpect(jsonPath("$.client.fullName", is("Иван Иванов")))
                .andExpect(jsonPath("$.event.name", is("Концерт классической музыки")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));

        verify(ticketReservationService, times(1)).getById(1L);
    }

    @Test
    void getTicketReservationById_WithNonExistentId_ShouldReturn404() throws Exception {
        when(ticketReservationService.getById(999L))
                .thenThrow(new com.example.eventmanagement.exception.EntityNotFoundException(
                        "Бронь с id 999 не найдена"));

        mockMvc.perform(get("/api/ticketReservations/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("не найдена")));

        verify(ticketReservationService, times(1)).getById(999L);
    }

    @Test
    void createTicketReservation_WithValidData_ShouldReturnCreatedReservation() throws Exception {
        TicketReservationDoneDto createdReservation = new TicketReservationDoneDto(
                3L,
                clientDto,
                eventDto,
                3,
                BookingStatus.PENDING_CONFIRMATION,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(ticketReservationService.createReservation(any(TicketReservationCreateDto.class)))
                .thenReturn(createdReservation);

        mockMvc.perform(post("/api/ticketReservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.numberOfTickets", is(3)))
                .andExpect(jsonPath("$.bookingStatus", is("ожидает подтверждения")))
                .andExpect(jsonPath("$.client.fullName", is("Иван Иванов")))
                .andExpect(jsonPath("$.event.name", is("Концерт классической музыки")));

        verify(ticketReservationService, times(1)).createReservation(any(TicketReservationCreateDto.class));
    }

    @Test
    void createTicketReservation_WithInvalidData_ShouldReturn400() throws Exception {
        TicketReservationCreateDto invalidDto = new TicketReservationCreateDto(
                null,
                null,
                0,
                null
        );

        mockMvc.perform(post("/api/ticketReservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("numberOfTickets")));

        verify(ticketReservationService, never()).createReservation(any());
    }

    @Test
    void createTicketReservation_WithNoSeatsAvailable_ShouldReturn400() throws Exception {
        when(ticketReservationService.createReservation(any(TicketReservationCreateDto.class)))
                .thenThrow(new com.example.eventmanagement.exception.BusinessValidationException(
                        "Билеты на мероприятие Концерт классической музыки закончились"));

        mockMvc.perform(post("/api/ticketReservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BUSINESS_RULE_ERROR")))
                .andExpect(jsonPath("$.message", containsString("закончились")));

        verify(ticketReservationService, times(1)).createReservation(any(TicketReservationCreateDto.class));
    }

    @Test
    void createTicketReservation_WithNonExistentClient_ShouldReturn404() throws Exception {
        when(ticketReservationService.createReservation(any(TicketReservationCreateDto.class)))
                .thenThrow(new com.example.eventmanagement.exception.EntityNotFoundException(
                        "Клиент c id 999 не найден"));

        mockMvc.perform(post("/api/ticketReservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationCreateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Клиент")));

        verify(ticketReservationService, times(1)).createReservation(any(TicketReservationCreateDto.class));
    }

    @Test
    void cancelTicketReservation_WithValidId_ShouldReturnCanceledReservation() throws Exception {
        TicketReservationDoneDto canceledReservation = new TicketReservationDoneDto(
                1L,
                clientDto,
                eventDto,
                2,
                BookingStatus.CANCELED,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        when(ticketReservationService.cancelReservation(1L))
                .thenReturn(canceledReservation);

        mockMvc.perform(put("/api/ticketReservations/{id}/cancel", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.bookingStatus", is("отменено")));

        verify(ticketReservationService, times(1)).cancelReservation(1L);
    }

    @Test
    void cancelTicketReservation_WithNonExistentId_ShouldReturn404() throws Exception {
        when(ticketReservationService.cancelReservation(999L))
                .thenThrow(new com.example.eventmanagement.exception.EntityNotFoundException(
                        "Резервация по id 999 не найдена"));

        mockMvc.perform(put("/api/ticketReservations/{id}/cancel", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));

        verify(ticketReservationService, times(1)).cancelReservation(999L);
    }

    @Test
    void cancelTicketReservation_WithEventStarted_ShouldReturn400() throws Exception {
        when(ticketReservationService.cancelReservation(1L))
                .thenThrow(new com.example.eventmanagement.exception.BusinessValidationException(
                        "Отмена резервации по id 1 невозможна позже чем за день до начала мероприятия"));

        mockMvc.perform(put("/api/ticketReservations/{id}/cancel", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BUSINESS_RULE_ERROR")))
                .andExpect(jsonPath("$.message", containsString("невозможна")));

        verify(ticketReservationService, times(1)).cancelReservation(1L);
    }

    @Test
    void confirmTicketReservation_WithValidId_ShouldReturnConfirmedReservation() throws Exception {
        TicketReservationDoneDto confirmedReservation = new TicketReservationDoneDto(
                1L,
                clientDto,
                eventDto,
                2,
                BookingStatus.CONFIRMED,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        when(ticketReservationService.confirmReservation(1L))
                .thenReturn(confirmedReservation);

        mockMvc.perform(put("/api/ticketReservations/{id}/confirm", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.bookingStatus", is("подтверждено")));

        verify(ticketReservationService, times(1)).confirmReservation(1L);
    }

    @Test
    void confirmTicketReservation_WithNonExistentId_ShouldReturn404() throws Exception {
        when(ticketReservationService.confirmReservation(999L))
                .thenThrow(new com.example.eventmanagement.exception.EntityNotFoundException(
                        "Резервация по id 999 не найдено"));

        mockMvc.perform(put("/api/ticketReservations/{id}/confirm", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));

        verify(ticketReservationService, times(1)).confirmReservation(999L);
    }

    @Test
    void confirmTicketReservation_WithAlreadyCanceled_ShouldReturn400() throws Exception {
        when(ticketReservationService.confirmReservation(1L))
                .thenThrow(new com.example.eventmanagement.exception.BusinessValidationException(
                        "Подтверждение резервации по id 1 невозможно после отмены бронирования"));

        mockMvc.perform(put("/api/ticketReservations/{id}/confirm", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BUSINESS_RULE_ERROR")))
                .andExpect(jsonPath("$.message", containsString("невозможно")));

        verify(ticketReservationService, times(1)).confirmReservation(1L);
    }

    @Test
    void cleanupOldCanceledReservations_WithReservationsToClean_ShouldReturnCount() throws Exception {
        when(ticketReservationService.cleanupOldCanceledReservations())
                .thenReturn(5);

        mockMvc.perform(post("/api/ticketReservations/cleanup/canceled-reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount", is(5)))
                .andExpect(jsonPath("$.message", containsString("Удалено 5 бронирований")));

        verify(ticketReservationService, times(1)).cleanupOldCanceledReservations();
    }

    @Test
    void cleanupOldCanceledReservations_WithNoReservations_ShouldReturnZero() throws Exception {
        when(ticketReservationService.cleanupOldCanceledReservations())
                .thenReturn(0);

        mockMvc.perform(post("/api/ticketReservations/cleanup/canceled-reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount", is(0)))
                .andExpect(jsonPath("$.message", containsString("Нет старых отмененных бронирований")));

        verify(ticketReservationService, times(1)).cleanupOldCanceledReservations();
    }

    @Test
    void getAllTicketReservations_WithEmptyDatabase_ShouldReturnEmptyList() throws Exception {
        when(ticketReservationService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/ticketReservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(ticketReservationService, times(1)).getAll();
    }

    @Test
    void createTicketReservation_WithMissingRequiredFields_ShouldReturn400() throws Exception {
        String invalidJson = """
        {
            "clientId": 1,
            "eventId": 1
        }
        """;

        mockMvc.perform(post("/api/ticketReservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(ticketReservationService, never()).createReservation(any());
    }

    @Test
    void createTicketReservation_WithInvalidNumberOfTickets_ShouldReturn400() throws Exception {
        TicketReservationCreateDto invalidDto = new TicketReservationCreateDto(
                1L,
                1L,
                -1,
                BookingStatus.PENDING_CONFIRMATION
        );

        mockMvc.perform(post("/api/ticketReservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("numberOfTickets")));

        verify(ticketReservationService, never()).createReservation(any());
    }

    @Test
    void confirmTicketReservation_WithNoSeatsAvailable_ShouldReturn400() throws Exception {
        when(ticketReservationService.confirmReservation(1L))
                .thenThrow(new com.example.eventmanagement.exception.BusinessValidationException(
                        "Недостаточно мест для подтверждения брони по id 1"));

        mockMvc.perform(put("/api/ticketReservations/{id}/confirm", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BUSINESS_RULE_ERROR")))
                .andExpect(jsonPath("$.message", containsString("Недостаточно мест")));

        verify(ticketReservationService, times(1)).confirmReservation(1L);
    }

    @Test
    void createTicketReservation_WithCanceledEvent_ShouldReturn400() throws Exception {
        when(ticketReservationService.createReservation(any(TicketReservationCreateDto.class)))
                .thenThrow(new com.example.eventmanagement.exception.BusinessValidationException(
                        "Бронирование билетов для мероприятия Концерт классической музыки закрылось"));

        mockMvc.perform(post("/api/ticketReservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BUSINESS_RULE_ERROR")))
                .andExpect(jsonPath("$.message", containsString("закрылось")));

        verify(ticketReservationService, times(1)).createReservation(any(TicketReservationCreateDto.class));
    }
}