package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.service.ClientService;
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

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClientService clientService;

    private ClientDto clientDto1;
    private ClientDto clientDto2;
    private ClientDoneDto clientDoneDto;
    private ClientCreateWithDependenciesDto clientCreateDto;
    private ClientCreateDto clientUpdateDto;
    private PassportCreateDto passportDto;

    @BeforeEach
    void setUp() {
        passportDto = new PassportCreateDto("1234", "567890");

        clientDto1 = new ClientDto(
                1L,
                "Иван Иванов",
                "+79123456789",
                "ivan@mail.ru"
        );

        clientDto2 = new ClientDto(
                2L,
                "Петр Петров",
                "+79123456780",
                "petr@mail.ru"
        );

        clientDoneDto = new ClientDoneDto(
                1L,
                "Иван Иванов",
                "+79123456789",
                "ivan@mail.ru",
                passportDto,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        clientCreateDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов",
                "+79123456789",
                "ivan@mail.ru",
                passportDto
        );

        clientUpdateDto = new ClientCreateDto(
                "Иван Иванов Обновленный",
                "+79123456799",
                "ivan.new@mail.ru"
        );
    }

    @Test
    void getAllClients_ShouldReturnListOfClients() throws Exception {
        List<ClientDto> clients = Arrays.asList(clientDto1, clientDto2);
        when(clientService.getAll()).thenReturn(clients);

        mockMvc.perform(get("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].fullName", is("Иван Иванов")))
                .andExpect(jsonPath("$[0].email", is("ivan@mail.ru")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].fullName", is("Петр Петров")));

        verify(clientService, times(1)).getAll();
    }

    @Test
    void getClientById_WithValidId_ShouldReturnClient() throws Exception {
        when(clientService.getById(1L)).thenReturn(clientDoneDto);

        mockMvc.perform(get("/api/clients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fullName", is("Иван Иванов")))
                .andExpect(jsonPath("$.phoneNumber", is("+79123456789")))
                .andExpect(jsonPath("$.email", is("ivan@mail.ru")))
                .andExpect(jsonPath("$.passport.series", is("1234")))
                .andExpect(jsonPath("$.passport.number", is("567890")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));

        verify(clientService, times(1)).getById(1L);
    }

    @Test
    void getClientById_WithNonExistentId_ShouldReturn404() throws Exception {
        when(clientService.getById(999L))
                .thenThrow(new com.example.eventmanagement.exception.EntityNotFoundException("Клиент с id 999 не найден"));

        mockMvc.perform(get("/api/clients/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Клиент с id 999 не найден")));

        verify(clientService, times(1)).getById(999L);
    }

    @Test
    void searchClients_WithSearchTerm_ShouldReturnMatchingClients() throws Exception {
        List<ClientDto> searchResults = Arrays.asList(clientDto1);
        when(clientService.searchClients("Иван")).thenReturn(searchResults);

        mockMvc.perform(get("/api/clients/search")
                        .param("searchTerm", "Иван")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is("Иван Иванов")));

        verify(clientService, times(1)).searchClients("Иван");
    }

    @Test
    void searchClients_WithEmptySearchTerm_ShouldReturnAllClients() throws Exception {
        List<ClientDto> allClients = Arrays.asList(clientDto1, clientDto2);
        when(clientService.searchClients("")).thenReturn(allClients);

        mockMvc.perform(get("/api/clients/search")
                        .param("searchTerm", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(clientService, times(1)).searchClients("");
    }

    @Test
    void createClient_WithValidData_ShouldReturnCreatedClient() throws Exception {
        ClientDoneDto createdClient = new ClientDoneDto(
                1L,
                "Иван Иванов",
                "+79123456789",
                "ivan@mail.ru",
                passportDto,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(clientService.createClient(any(ClientCreateWithDependenciesDto.class)))
                .thenReturn(createdClient);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fullName", is("Иван Иванов")))
                .andExpect(jsonPath("$.email", is("ivan@mail.ru")))
                .andExpect(jsonPath("$.passport.series", is("1234")))
                .andExpect(jsonPath("$.passport.number", is("567890")));

        verify(clientService, times(1)).createClient(any(ClientCreateWithDependenciesDto.class));
    }

    @Test
    void createClient_WithInvalidData_ShouldReturn400() throws Exception {
        ClientCreateWithDependenciesDto invalidDto = new ClientCreateWithDependenciesDto(
                "",
                "invalid-phone",
                "invalid-email",
                new PassportCreateDto("12", "123")
        );

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("fullName")))
                .andExpect(jsonPath("$.message", containsString("phoneNumber")))
                .andExpect(jsonPath("$.message", containsString("email")));

        verify(clientService, never()).createClient(any());
    }

    @Test
    void createClient_WithDuplicateEmail_ShouldReturn409() throws Exception {
        when(clientService.createClient(any(ClientCreateWithDependenciesDto.class)))
                .thenThrow(new com.example.eventmanagement.exception.DuplicateEntityException(
                        "Клиент c таким email ivan@mail.ru уже существует"));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientCreateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("DUPLICATE_ENTITY")))
                .andExpect(jsonPath("$.message", containsString("уже существует")));

        verify(clientService, times(1)).createClient(any(ClientCreateWithDependenciesDto.class));
    }

    @Test
    void updateClient_WithValidData_ShouldReturnUpdatedClient() throws Exception {
        ClientDoneDto updatedClient = new ClientDoneDto(
                1L,
                "Иван Иванов Обновленный",
                "+79123456799",
                "ivan.new@mail.ru",
                passportDto,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        when(clientService.updateClientBasicInfo(eq(1L), any(ClientCreateDto.class)))
                .thenReturn(updatedClient);

        mockMvc.perform(put("/api/clients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fullName", is("Иван Иванов Обновленный")))
                .andExpect(jsonPath("$.phoneNumber", is("+79123456799")))
                .andExpect(jsonPath("$.email", is("ivan.new@mail.ru")));

        verify(clientService, times(1)).updateClientBasicInfo(eq(1L), any(ClientCreateDto.class));
    }

    @Test
    void updateClient_WithInvalidData_ShouldReturn400() throws Exception {
        ClientCreateDto invalidDto = new ClientCreateDto(
                "",
                "invalid",
                "invalid-email"
        );

        mockMvc.perform(put("/api/clients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));

        verify(clientService, never()).updateClientBasicInfo(any(), any());
    }

    @Test
    void updateClient_WithNonExistentId_ShouldReturn404() throws Exception {
        when(clientService.updateClientBasicInfo(eq(999L), any(ClientCreateDto.class)))
                .thenThrow(new com.example.eventmanagement.exception.EntityNotFoundException(
                        "Клиент с id 999 не найден"));

        mockMvc.perform(put("/api/clients/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientUpdateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));

        verify(clientService, times(1)).updateClientBasicInfo(eq(999L), any(ClientCreateDto.class));
    }

    @Test
    void updatePassport_WithValidData_ShouldReturnUpdatedClient() throws Exception {
        PassportCreateDto newPassportDto = new PassportCreateDto("4321", "098765");
        ClientDoneDto updatedClient = new ClientDoneDto(
                1L,
                "Иван Иванов",
                "+79123456789",
                "ivan@mail.ru",
                newPassportDto,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        when(clientService.replacePassport(eq(1L), any(PassportCreateDto.class)))
                .thenReturn(updatedClient);

        mockMvc.perform(put("/api/clients/{id}/passport", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPassportDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.passport.series", is("4321")))
                .andExpect(jsonPath("$.passport.number", is("098765")));

        verify(clientService, times(1)).replacePassport(eq(1L), any(PassportCreateDto.class));
    }

    @Test
    void updatePassport_WithInvalidPassportData_ShouldReturn400() throws Exception {
        PassportCreateDto invalidPassportDto = new PassportCreateDto("12", "123");

        mockMvc.perform(put("/api/clients/{id}/passport", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPassportDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));

        verify(clientService, never()).replacePassport(any(), any());
    }

    @Test
    void updatePassport_WithDuplicatePassport_ShouldReturn409() throws Exception {
        PassportCreateDto duplicatePassportDto = new PassportCreateDto("4321", "098765");

        when(clientService.replacePassport(eq(1L), any(PassportCreateDto.class)))
                .thenThrow(new com.example.eventmanagement.exception.DuplicateEntityException(
                        "Паспорт 4321 098765 уже существует"));

        mockMvc.perform(put("/api/clients/{id}/passport", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicatePassportDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("DUPLICATE_ENTITY")));

        verify(clientService, times(1)).replacePassport(eq(1L), any(PassportCreateDto.class));
    }

    @Test
    void deleteClient_WithValidId_ShouldReturn204() throws Exception {
        doNothing().when(clientService).deleteClient(1L);

        mockMvc.perform(delete("/api/clients/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(clientService, times(1)).deleteClient(1L);
    }

    @Test
    void deleteClient_WithNonExistentId_ShouldReturn404() throws Exception {
        doThrow(new com.example.eventmanagement.exception.EntityNotFoundException(
                "Клиент с id 999 не найден"))
                .when(clientService).deleteClient(999L);

        mockMvc.perform(delete("/api/clients/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).deleteClient(999L);
    }

    @Test
    void deleteClient_WithActiveReservations_ShouldReturn400() throws Exception {
        doThrow(new com.example.eventmanagement.exception.BusinessValidationException(
                "Этого клиента с id 1 нельзя удалить, у него еще есть активные бронирования"))
                .when(clientService).deleteClient(1L);

        mockMvc.perform(delete("/api/clients/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BUSINESS_RULE_ERROR")))
                .andExpect(jsonPath("$.message", containsString("нельзя удалить")));

        verify(clientService, times(1)).deleteClient(1L);
    }
    @Test
    void createClient_WithMissingRequiredFields_ShouldReturn400() throws Exception {
        String invalidJson = """
        {
            "phoneNumber": "+79123456789",
            "passport": {
                "series": "1234",
                "number": "567890"
            }
        }
        """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any());
    }

    @Test
    void createClient_WithInvalidPhoneFormat_ShouldReturn400() throws Exception {
        ClientCreateWithDependenciesDto dtoWithInvalidPhone = new ClientCreateWithDependenciesDto(
                "Иван Иванов",
                "89123456789",
                "ivan@mail.ru",
                passportDto
        );

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithInvalidPhone)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("phoneNumber")));

        verify(clientService, never()).createClient(any());
    }

    @Test
    void getAllClients_WithEmptyDatabase_ShouldReturnEmptyList() throws Exception {
        when(clientService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(clientService, times(1)).getAll();
    }

    @Test
    void updateClient_WithDuplicatePhone_ShouldReturn409() throws Exception {
        when(clientService.updateClientBasicInfo(eq(1L), any(ClientCreateDto.class)))
                .thenThrow(new com.example.eventmanagement.exception.DuplicateEntityException(
                        "Клиент c таким телефоном +79123456799 уже существует"));

        mockMvc.perform(put("/api/clients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientUpdateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("DUPLICATE_ENTITY")))
                .andExpect(jsonPath("$.message", containsString("телефоном")));

        verify(clientService, times(1)).updateClientBasicInfo(eq(1L), any(ClientCreateDto.class));
    }

    @Test
    void updateClient_WithDuplicateEmail_ShouldReturn409() throws Exception {
        when(clientService.updateClientBasicInfo(eq(1L), any(ClientCreateDto.class)))
                .thenThrow(new com.example.eventmanagement.exception.DuplicateEntityException(
                        "Клиент c таким email ivan.new@mail.ru уже существует"));

        mockMvc.perform(put("/api/clients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientUpdateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("DUPLICATE_ENTITY")))
                .andExpect(jsonPath("$.message", containsString("email")));

        verify(clientService, times(1)).updateClientBasicInfo(eq(1L), any(ClientCreateDto.class));
    }
}