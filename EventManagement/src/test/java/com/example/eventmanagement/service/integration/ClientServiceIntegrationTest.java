package com.example.eventmanagement.service.integration;

import com.example.eventmanagement.dto.ClientCreateWithDependenciesDto;
import com.example.eventmanagement.dto.ClientDoneDto;
import com.example.eventmanagement.dto.PassportCreateDto;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.exception.DuplicateEntityException;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.PassportRepository;
import com.example.eventmanagement.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClientServiceIntegrationTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PassportRepository passportRepository;

    @Test
    void createClient_WithValidData_SavesToDatabase() {
        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );

        ClientDoneDto result = clientService.createClient(createDto);

        assertNotNull(result.id());
        assertEquals("Иван Иванов", result.fullName());

        Client savedClient = clientRepository.findById(result.id()).orElseThrow();
        assertEquals("Иван Иванов", savedClient.getFullName());
        assertEquals("+79123456789", savedClient.getPhoneNumber());
        assertNotNull(savedClient.getPassport());
        assertEquals("1234", savedClient.getPassport().getSeries());
        assertEquals("567890", savedClient.getPassport().getNumber());
    }

    @Test
    void createClient_WithDuplicatePassport_ThrowsException() {
        ClientCreateWithDependenciesDto firstDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );

        ClientCreateWithDependenciesDto secondDto = new ClientCreateWithDependenciesDto(
                "Петр Петров", "+79123456780", "petr@mail.ru",
                new PassportCreateDto("1234", "567890")
        );

        clientService.createClient(firstDto);

        assertThrows(DuplicateEntityException.class, () -> {
            clientService.createClient(secondDto);
        });
    }

    @Test
    void deleteClient_WithNoReservations_RemovesFromDatabase() {
        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );

        ClientDoneDto created = clientService.createClient(createDto);
        assertTrue(clientRepository.existsById(created.id()));

        clientService.deleteClient(created.id());

        assertFalse(clientRepository.existsById(created.id()));
        assertFalse(passportRepository.existsBySeriesAndNumber("1234", "567890"));
    }

    @Test
    void replacePassport_UpdatesPassportInDatabase() {
        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );

        ClientDoneDto created = clientService.createClient(createDto);
        PassportCreateDto newPassportDto = new PassportCreateDto("4321", "098765");

        ClientDoneDto updated = clientService.replacePassport(created.id(), newPassportDto);

        Client client = clientRepository.findById(created.id()).orElseThrow();
        assertEquals("4321", client.getPassport().getSeries());
        assertEquals("098765", client.getPassport().getNumber());
        assertFalse(passportRepository.existsBySeriesAndNumber("1234", "567890"));
    }
}