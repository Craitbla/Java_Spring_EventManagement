package com.example.eventmanagement.controller.integration;

import com.example.eventmanagement.dto.ClientCreateWithDependenciesDto;
import com.example.eventmanagement.dto.ClientCreateDto;
import com.example.eventmanagement.dto.ClientDoneDto;
import com.example.eventmanagement.dto.PassportCreateDto;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.exception.DuplicateEntityException;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.PassportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PassportRepository passportRepository;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
        passportRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        clientRepository.deleteAll();
        passportRepository.deleteAll();
    }

    @Test
    void getAllClients_shouldReturnEmptyListWhenNoClients() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllClients_shouldReturnListOfClientsWhenClientsExist() throws Exception {
        // Создаем двух клиентов
        ClientCreateWithDependenciesDto client1 = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );
        ClientCreateWithDependenciesDto client2 = new ClientCreateWithDependenciesDto(
                "Петр Петров", "+79876543210", "petr@mail.ru",
                new PassportCreateDto("4321", "098765")
        );

        // Сохраняем клиентов через сервис
        ClientDoneDto createdClient1 = createClient(client1);
        ClientDoneDto createdClient2 = createClient(client2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fullName").value("Иван Иванов"))
                .andExpect(jsonPath("$[1].fullName").value("Петр Петров"));
    }

    @Test
    void getClientById_shouldReturnClientWhenClientExists() throws Exception {
        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );

        ClientDoneDto createdClient = createClient(createDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/clients/" + createdClient.id())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Иван Иванов"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"))
                .andExpect(jsonPath("$.phoneNumber").value("+79123456789"))
                .andExpect(jsonPath("$.passport.series").value("1234"))
                .andExpect(jsonPath("$.passport.number").value("567890"));
    }

    @Test
    void getClientById_shouldReturnNotFoundWhenClientDoesNotExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/clients/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Клиент с id 999 не найден"));
    }

    @Test
    void searchClient_shouldReturnClientsMatchingSearchTerm() throws Exception {
        // Создаем клиентов для поиска
        ClientCreateWithDependenciesDto client1 = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );
        ClientCreateWithDependenciesDto client2 = new ClientCreateWithDependenciesDto(
                "Петр Петров", "+79876543210", "petr@mail.ru",
                new PassportCreateDto("4321", "098765")
        );
        ClientCreateWithDependenciesDto client3 = new ClientCreateWithDependenciesDto(
                "Александр Сидоров", "+79998887766", "alex@mail.ru",
                new PassportCreateDto("5555", "666666")
        );

        createClient(client1);
        createClient(client2);
        createClient(client3);

        // Поиск по имени
        mockMvc.perform(MockMvcRequestBuilders.get("/api/clients/search")
                        .param("searchTerm", "Иван")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Иван Иванов"));

        // Поиск по телефону
        mockMvc.perform(MockMvcRequestBuilders.get("/api/clients/search")
                        .param("searchTerm", "9876543210")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Петр Петров"));

        // Поиск по email
        mockMvc.perform(MockMvcRequestBuilders.get("/api/clients/search")
                        .param("searchTerm", "alex")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Александр Сидоров"));
    }

    @Test
    void createClient_shouldCreateClientWithValidData() throws Exception {
        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Иван Иванов"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"))
                .andExpect(jsonPath("$.phoneNumber").value("+79123456789"))
                .andExpect(jsonPath("$.passport.series").value("1234"))
                .andExpect(jsonPath("$.passport.number").value("567890"));

        // Проверяем, что клиент сохранен в базе
        List<Client> clients = clientRepository.findAll();
        assertThat(clients).hasSize(1);
        Client savedClient = clients.get(0);
        assertThat(savedClient.getFullName()).isEqualTo("Иван Иванов");
        assertThat(savedClient.getEmail()).isEqualTo("ivan@mail.ru");
        assertThat(savedClient.getPhoneNumber()).isEqualTo("+79123456789");
        assertThat(savedClient.getPassport().getSeries()).isEqualTo("1234");
        assertThat(savedClient.getPassport().getNumber()).isEqualTo("567890");
    }

    @Test
    void createClient_shouldReturnConflictWhenDuplicateEmail() throws Exception {
        // Создаем первого клиента
        ClientCreateWithDependenciesDto firstClient = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );
        createClient(firstClient);

        // Пытаемся создать второго клиента с тем же email
        ClientCreateWithDependenciesDto secondClient = new ClientCreateWithDependenciesDto(
                "Петр Петров", "+79876543210", "ivan@mail.ru", // тот же email
                new PassportCreateDto("4321", "098765")
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondClient)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_ENTITY"))
                .andExpect(jsonPath("$.message").value("Клиент c таким email ivan@mail.ru уже существует"));
    }

    @Test
    void createClient_shouldReturnConflictWhenDuplicatePhone() throws Exception {
        // Создаем первого клиента
        ClientCreateWithDependenciesDto firstClient = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );
        createClient(firstClient);

        // Пытаемся создать второго клиента с тем же телефоном
        ClientCreateWithDependenciesDto secondClient = new ClientCreateWithDependenciesDto(
                "Петр Петров", "+79123456789", "petr@mail.ru", // тот же телефон
                new PassportCreateDto("4321", "098765")
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondClient)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_ENTITY"))
                .andExpect(jsonPath("$.message").value("Клиент c таким телефоном +79123456789 уже существует"));
    }

    @Test
    void createClient_shouldReturnConflictWhenDuplicatePassport() throws Exception {
        // Создаем первого клиента
        ClientCreateWithDependenciesDto firstClient = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );
        createClient(firstClient);

        // Пытаемся создать второго клиента с тем же паспортом
        ClientCreateWithDependenciesDto secondClient = new ClientCreateWithDependenciesDto(
                "Петр Петров", "+79876543210", "petr@mail.ru",
                new PassportCreateDto("1234", "567890") // тот же паспорт
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondClient)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_ENTITY"))
                .andExpect(jsonPath("$.message").value("Клиент c таким паспортом 5678901234 уже существует"));
    }

    @Test
    void updateClient_shouldUpdateClientBasicInfo() throws Exception {
        // Создаем клиента
        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );
        ClientDoneDto createdClient = createClient(createDto);

        // Обновляем базовую информацию
        ClientCreateDto updateDto = new ClientCreateDto(
                "Иван Петров", "+79223334455", "ivan_new@mail.ru"
        );

        mockMvc.perform(MockMvcRequestBuilders.put("/api/clients/" + createdClient.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Иван Петров"))
                .andExpect(jsonPath("$.email").value("ivan_new@mail.ru"))
                .andExpect(jsonPath("$.phoneNumber").value("+79223334455"));

        // Проверяем, что данные обновлены в базе
        Client updatedClient = clientRepository.findById(createdClient.id()).orElseThrow();
        assertThat(updatedClient.getFullName()).isEqualTo("Иван Петров");
        assertThat(updatedClient.getEmail()).isEqualTo("ivan_new@mail.ru");
        assertThat(updatedClient.getPhoneNumber()).isEqualTo("+79223334455");
        // Проверяем, что паспорт остался без изменений
        assertThat(updatedClient.getPassport().getSeries()).isEqualTo("1234");
        assertThat(updatedClient.getPassport().getNumber()).isEqualTo("567890");
    }

    @Test
    void updateClient_shouldReturnConflictWhenDuplicateEmailOnUpdate() throws Exception {
        // Создаем двух клиентов
        ClientCreateWithDependenciesDto firstClient = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );
        ClientCreateWithDependenciesDto secondClient = new ClientCreateWithDependenciesDto(
                "Петр Петров", "+79876543210", "petr@mail.ru",
                new PassportCreateDto("4321", "098765")
        );

        ClientDoneDto createdFirst = createClient(firstClient);
        createClient(secondClient);

        // Пытаемся обновить первого клиента, используя email второго
        ClientCreateDto updateDto = new ClientCreateDto(
                "Иван Иванов", "+79223334455", "petr@mail.ru" // email второго клиента
        );

        mockMvc.perform(MockMvcRequestBuilders.put("/api/clients/" + createdFirst.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_ENTITY"))
                .andExpect(jsonPath("$.message").value("Клиент c таким email petr@mail.ru уже существует"));
    }

    @Test
    void updatePassport_shouldUpdateClientPassport() throws Exception {
        // Создаем клиента
        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );
        ClientDoneDto createdClient = createClient(createDto);

        // Обновляем паспорт
        PassportCreateDto newPassportDto = new PassportCreateDto("4321", "111111");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/clients/" + createdClient.id() + "/passport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPassportDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passport.series").value("4321"))
                .andExpect(jsonPath("$.passport.number").value("111111"));

        // Проверяем, что паспорт обновлен в базе
        Client updatedClient = clientRepository.findById(createdClient.id()).orElseThrow();
        assertThat(updatedClient.getPassport().getSeries()).isEqualTo("4321");
        assertThat(updatedClient.getPassport().getNumber()).isEqualTo("111111");
        // Проверяем, что старый паспорт удален
        assertThat(passportRepository.existsBySeriesAndNumber("1234", "567890")).isFalse();
    }

    @Test
    void updatePassport_shouldReturnConflictWhenDuplicatePassportOnUpdate() throws Exception {
        // Создаем двух клиентов
        ClientCreateWithDependenciesDto firstClient = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );
        ClientCreateWithDependenciesDto secondClient = new ClientCreateWithDependenciesDto(
                "Петр Петров", "+79876543210", "petr@mail.ru",
                new PassportCreateDto("4321", "098765")
        );

        ClientDoneDto createdFirst = createClient(firstClient);
        createClient(secondClient);

        // Пытаемся обновить паспорт первого клиента, используя паспорт второго
        PassportCreateDto newPassportDto = new PassportCreateDto("4321", "098765"); // паспорт второго клиента

        mockMvc.perform(MockMvcRequestBuilders.put("/api/clients/" + createdFirst.id() + "/passport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPassportDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_ENTITY"))
                .andExpect(jsonPath("$.message").value("Паспорт 4321 098765 уже существует"));
    }

    @Test
    void deleteClient_shouldDeleteClientWhenNoActiveReservations() throws Exception {
        // Создаем клиента
        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );
        ClientDoneDto createdClient = createClient(createDto);

        // Удаляем клиента
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/clients/" + createdClient.id())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Проверяем, что клиент и паспорт удалены
        assertThat(clientRepository.existsById(createdClient.id())).isFalse();
        assertThat(passportRepository.existsBySeriesAndNumber("1234", "567890")).isFalse();
    }

    // Вспомогательный метод для создания клиента через сервис
    private ClientDoneDto createClient(ClientCreateWithDependenciesDto dto) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, ClientDoneDto.class);
    }
}