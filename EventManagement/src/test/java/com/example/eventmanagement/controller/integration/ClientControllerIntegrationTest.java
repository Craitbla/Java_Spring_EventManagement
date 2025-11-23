package com.example.eventmanagement.controller.integration;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.PassportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PassportRepository passportRepository;

    private Client testClient;
    private Passport testPassport;

    @BeforeEach
    void setUp() {
        passportRepository.deleteAll();
        clientRepository.deleteAll();

        testPassport = new Passport("1234", "567890");
        testPassport = passportRepository.save(testPassport);

        testClient = new Client("Иван Иванов", "+79123456789", "ivan@mail.ru", testPassport);
        testClient = clientRepository.save(testClient);
    }

    @Test
    void getAllClients_ShouldReturnAllClients() throws Exception {
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testClient.getId()))
                .andExpect(jsonPath("$[0].fullName").value("Иван Иванов"))
                .andExpect(jsonPath("$[0].email").value("ivan@mail.ru"));
    }

    @Test
    void getClientById_ShouldReturnClient() throws Exception {
        mockMvc.perform(get("/api/clients/{id}", testClient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClient.getId()))
                .andExpect(jsonPath("$.fullName").value("Иван Иванов"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));
    }

    @Test
    void getClientById_WhenNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/clients/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchClients_ShouldReturnMatchingClients() throws Exception {
        mockMvc.perform(get("/api/clients/search")
                        .param("searchTerm", "Иван"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Иван Иванов"));
    }

    @Test
    void createClient_ShouldCreateNewClient() throws Exception {
        ClientCreateWithDependenciesDto newClient = new ClientCreateWithDependenciesDto(
                "Петр Петров", "+79123456780", "petr@mail.ru",
                new PassportCreateDto("4321", "098765")
        );

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Петр Петров"))
                .andExpect(jsonPath("$.email").value("petr@mail.ru"));

        assertTrue(clientRepository.findByEmail("petr@mail.ru").isPresent());
    }

    @Test
    void createClient_WithDuplicateEmail_ShouldReturnConflict() throws Exception {
        ClientCreateWithDependenciesDto duplicateClient = new ClientCreateWithDependenciesDto(
                "Другой Иван", "+79123456781", "ivan@mail.ru",
                new PassportCreateDto("5555", "666666")
        );

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateClient)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateClient_ShouldUpdateExistingClient() throws Exception {
        ClientCreateDto updateDto = new ClientCreateDto("Иван Обновленный", "+79123456789", "ivan.new@mail.ru");

        mockMvc.perform(put("/api/clients/{id}", testClient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Иван Обновленный"))
                .andExpect(jsonPath("$.email").value("ivan.new@mail.ru"));

        Client updatedClient = clientRepository.findById(testClient.getId()).orElseThrow();
        assertEquals("Иван Обновленный", updatedClient.getFullName());
        assertEquals("ivan.new@mail.ru", updatedClient.getEmail());
    }

    @Test
    void updatePassport_ShouldUpdateClientPassport() throws Exception {
        PassportCreateDto newPassport = new PassportCreateDto("9999", "888888");

        mockMvc.perform(put("/api/clients/{id}/passport", testClient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPassport)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClient.getId()));

        Client updatedClient = clientRepository.findById(testClient.getId()).orElseThrow();
        assertEquals("9999", updatedClient.getPassport().getSeries());
        assertEquals("888888", updatedClient.getPassport().getNumber());
    }

    @Test
    void deleteClient_ShouldDeleteClient() throws Exception {
        mockMvc.perform(delete("/api/clients/{id}", testClient.getId()))
                .andExpect(status().isNoContent());

        assertFalse(clientRepository.existsById(testClient.getId()));
    }

    @Test
    void deleteClient_WhenNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/clients/999"))
                .andExpect(status().isNotFound());
    }
}
