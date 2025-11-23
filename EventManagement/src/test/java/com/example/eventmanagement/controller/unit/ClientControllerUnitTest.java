//package com.example.eventmanagement.controller.unit;
//
//import com.example.eventmanagement.controller.ClientController;
//import com.example.eventmanagement.dto.*;
//import com.example.eventmanagement.exception.EntityNotFoundException;
//import com.example.eventmanagement.service.ClientService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(ClientController.class)
//class ClientControllerUnitTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private ClientService clientService;
//
//    private final ClientDto clientDto = new ClientDto(1L, "Иван Иванов", "+79123456789", "ivan@mail.ru");
//    private final ClientDoneDto clientDoneDto = new ClientDoneDto(1L, "Иван Иванов", "+79123456789", "ivan@mail.ru",
//            new PassportCreateDto("1234", "567890"), LocalDateTime.now(), LocalDateTime.now());
//
//    @Test
//    void getAllClients_ShouldReturnClientsList() throws Exception {
//        when(clientService.getAll()).thenReturn(List.of(clientDto));
//
//        mockMvc.perform(get("/api/clients"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value(1L))
//                .andExpect(jsonPath("$[0].fullName").value("Иван Иванов"));
//    }
//
//    @Test
//    void getClientById_ShouldReturnClient() throws Exception {
//        when(clientService.getById(1L)).thenReturn(clientDoneDto);
//
//        mockMvc.perform(get("/api/clients/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.fullName").value("Иван Иванов"));
//    }
//
//    @Test
//    void getClientById_WhenNotFound_ShouldReturn404() throws Exception {
//        when(clientService.getById(1L)).thenThrow(new EntityNotFoundException("Клиент не найден"));
//
//        mockMvc.perform(get("/api/clients/1"))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void searchClients_ShouldReturnFilteredClients() throws Exception {
//        when(clientService.searchClients("Иван")).thenReturn(List.of(clientDto));
//
//        mockMvc.perform(get("/api/clients/search")
//                        .param("searchTerm", "Иван"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].fullName").value("Иван Иванов"));
//    }
//
//    @Test
//    void createClient_ShouldReturnCreatedClient() throws Exception {
//        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
//                "Иван Иванов", "+79123456789", "ivan@mail.ru",
//                new PassportCreateDto("1234", "567890")
//        );
//
//        when(clientService.createClient(any(ClientCreateWithDependenciesDto.class)))
//                .thenReturn(clientDoneDto);
//
//        mockMvc.perform(post("/api/clients")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createDto)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.fullName").value("Иван Иванов"));
//    }
//
//    @Test
//    void createClient_WithInvalidData_ShouldReturnBadRequest() throws Exception {
//        ClientCreateWithDependenciesDto invalidDto = new ClientCreateWithDependenciesDto(
//                "", "invalid-phone", "invalid-email",
//                new PassportCreateDto("12", "123")
//        );
//
//        mockMvc.perform(post("/api/clients")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidDto)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void updateClient_ShouldReturnUpdatedClient() throws Exception {
//        ClientCreateDto updateDto = new ClientCreateDto("Петр Петров", "+79123456780", "petr@mail.ru");
//        ClientDoneDto updatedClient = new ClientDoneDto(1L, "Петр Петров", "+79123456780", "petr@mail.ru",
//                new PassportCreateDto("1234", "567890"), LocalDateTime.now(), LocalDateTime.now());
//
//        when(clientService.updateClientBasicInfo(anyLong(), any(ClientCreateDto.class)))
//                .thenReturn(updatedClient);
//
//        mockMvc.perform(put("/api/clients/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.fullName").value("Петр Петров"))
//                .andExpect(jsonPath("$.email").value("petr@mail.ru"));
//    }
//
//    @Test
//    void updatePassport_ShouldReturnClientWithNewPassport() throws Exception {
//        PassportCreateDto passportDto = new PassportCreateDto("4321", "098765");
//        when(clientService.replacePassport(anyLong(), any(PassportCreateDto.class)))
//                .thenReturn(clientDoneDto);
//
//        mockMvc.perform(put("/api/clients/1/passport")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(passportDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1L));
//    }
//
//    @Test
//    void deleteClient_ShouldReturnNoContent() throws Exception {
//        mockMvc.perform(delete("/api/clients/1"))
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    void deleteClient_WhenNotFound_ShouldReturn404() throws Exception {
//        doThrow(new EntityNotFoundException("Клиент не найден"))
//                .when(clientService).deleteClient(1L);
//
//        mockMvc.perform(delete("/api/clients/1"))
//                .andExpect(status().isNotFound());
//    }
//}