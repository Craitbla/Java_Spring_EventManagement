//package com.example.eventmanagement.exception;
//
//import com.example.eventmanagement.dto.EventCreateDto;
//import com.example.eventmanagement.enums.EventStatus;
//import com.example.eventmanagement.service.EventService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//
//import static org.hamcrest.Matchers.containsString;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest
//class GlobalExceptionHandlerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private EventService eventService;
//
//    @Test
//    void whenEntityNotFoundException_ShouldReturn404() throws Exception {
//        when(eventService.getById(999L))
//                .thenThrow(new com.example.eventmanagement.exception.EntityNotFoundException(
//                        "Мероприятие с id 999 не найдено"));
//
//        mockMvc.perform(get("/api/events/999")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
//                .andExpect(jsonPath("$.message", containsString("не найдено")));
//    }
//
//    @Test
//    void whenDuplicateEntityException_ShouldReturn409() throws Exception {
//        EventCreateDto eventCreateDto = new EventCreateDto(
//                "Концерт",
//                LocalDate.now().plusDays(10),
//                100,
//                BigDecimal.valueOf(1000),
//                EventStatus.PLANNED,
//                "Описание"
//        );
//
//        when(eventService.createEvent(any()))
//                .thenThrow(new com.example.eventmanagement.exception.DuplicateEntityException(
//                        "Такое мероприятие уже существует"));
//
//        mockMvc.perform(post("/api/events")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(eventCreateDto)))
//                .andExpect(status().isConflict())
//                .andExpect(jsonPath("$.error", is("DUPLICATE_ENTITY")))
//                .andExpect(jsonPath("$.message", containsString("уже существует")));
//    }
//
//    @Test
//    void whenBusinessValidationException_ShouldReturn400() throws Exception {
//        when(eventService.deleteEvent(1L))
//                .thenThrow(new com.example.eventmanagement.exception.BusinessValidationException(
//                        "Это мероприятие с id 1 нельзя удалить, оно еще проходит или будет проходить"));
//
//        mockMvc.perform(delete("/api/events/1"))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.error", is("BUSINESS_RULE_ERROR")));
//    }
//
//    @Test
//    void whenMethodArgumentNotValidException_ShouldReturn400() throws Exception {
//        String invalidJson = """
//            {
//                "name": "",
//                "date": "2024-12-31",
//                "numberOfSeats": 0,
//                "ticketPrice": -100
//            }
//            """;
//
//        mockMvc.perform(post("/api/events")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(invalidJson))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
//                .andExpect(jsonPath("$.message", containsString("name")))
//                .andExpect(jsonPath("$.message", containsString("numberOfSeats")))
//                .andExpect(jsonPath("$.message", containsString("ticketPrice")));
//    }
//
//    @Test
//    void whenGenericException_ShouldReturn500() throws Exception {
//        when(eventService.getAll())
//                .thenThrow(new RuntimeException("Внезапная ошибка"));
//
//        mockMvc.perform(get("/api/events"))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.error", is("INTERNAL_ERROR")))
//                .andExpect(jsonPath("$.message", is("Internal server error")));
//    }
//}