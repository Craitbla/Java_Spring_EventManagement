//package com.example.eventmanagement.integration;
//
//import com.example.eventmanagement.dto.ClientCreateWithDependenciesDto;
//import com.example.eventmanagement.dto.ClientDoneDto;
//import com.example.eventmanagement.dto.PassportCreateDto;
//import com.example.eventmanagement.entity.Client;
//import com.example.eventmanagement.entity.Event;
//import com.example.eventmanagement.entity.Passport;
//import com.example.eventmanagement.entity.TicketReservation;
//import com.example.eventmanagement.enums.BookingStatus;
//import com.example.eventmanagement.enums.EventStatus;
//import com.example.eventmanagement.exception.BusinessValidationException;
//import com.example.eventmanagement.exception.DuplicateEntityException;
//import com.example.eventmanagement.exception.EntityNotFoundException;
//import com.example.eventmanagement.repository.ClientRepository;
//import com.example.eventmanagement.repository.EventRepository;
//import com.example.eventmanagement.repository.TicketReservationRepository;
//import com.example.eventmanagement.service.ClientService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
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
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@SpringBootTest
//@Testcontainers
//@Transactional
//@ActiveProfiles("testcontainers") //1
//class ClientServiceIntegrationnTest extends BaseTestcontainersTest { //2
////
////    @Container                                //3
////    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
////            .withDatabaseName("testdb")
////            .withUsername("testuser")
////            .withPassword("testpass");
////
////    @DynamicPropertySource
////    static void configureProperties(DynamicPropertyRegistry registry) {
////        registry.add("spring.datasource.url", postgres::getJdbcUrl);
////        registry.add("spring.datasource.username", postgres::getUsername);
////        registry.add("spring.datasource.password", postgres::getPassword);
////        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
////    }
//
//    @Autowired
//    private ClientService clientService;
//
//    @Autowired
//    private ClientRepository clientRepository;
//
//    @Autowired
//    private EventRepository eventRepository;
//
//    @Autowired
//    private TicketReservationRepository ticketReservationRepository;
//
//    private Client testClient;
//    private Event testEvent;
//
//    @BeforeEach
//    void setUp() {
//        // Очищаем базу данных перед каждым тестом
//        ticketReservationRepository.deleteAll();
//        clientRepository.deleteAll();
//        eventRepository.deleteAll();
//
//        // Создаем тестового клиента
//        Passport passport = new Passport("1234", "567890");
//        testClient = new Client(
//                "Иван Иванов",
//                "+79123456789",
//                "ivan@mail.ru",
//                passport
//        );
//        testClient = clientRepository.save(testClient);
//
//        // Создаем тестовое мероприятие
//        testEvent = new Event(
//                "Концерт классической музыки",
//                LocalDate.now().plusDays(30),
//                100,
//                BigDecimal.valueOf(1500),
//                EventStatus.PLANNED,
//                "Концерт симфонического оркестра"
//        );
//        testEvent = eventRepository.save(testEvent);
//    }
//
//    @Test
//    void shouldCreateClientSuccessfully() {
//        // Given
//        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
//                "Петр Петров",
//                "+79123456780",
//                "petr@mail.ru",
//                new PassportCreateDto("4321", "098765")
//        );
//
//        // When
//        ClientDoneDto result = clientService.createClient(createDto);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.id()).isNotNull();
//        assertThat(result.fullName()).isEqualTo("Петр Петров");
//        assertThat(result.email()).isEqualTo("petr@mail.ru");
//        assertThat(result.passport().series()).isEqualTo("4321");
//        assertThat(result.passport().number()).isEqualTo("098765");
//        assertThat(result.createdAt()).isNotNull();
//        assertThat(result.updatedAt()).isNotNull();
//
//        // Проверяем, что клиент сохранен в БД
//        Client savedClient = clientRepository.findById(result.id()).orElseThrow();
//        assertThat(savedClient.getFullName()).isEqualTo("Петр Петров");
//        assertThat(savedClient.getPassport().getSeries()).isEqualTo("4321");
//    }
//
//    @Test
//    void shouldThrowDuplicateEntityExceptionWhenCreatingClientWithExistingEmail() {
//        // Given
//        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
//                "Другой Иван",
//                "+79123456781",
//                "ivan@mail.ru", // Дублирующий email
//                new PassportCreateDto("5555", "666666")
//        );
//
//        // When & Then
//        assertThatThrownBy(() -> clientService.createClient(createDto))
//                .isInstanceOf(DuplicateEntityException.class)
//                .hasMessageContaining("уже существует");
//    }
//
//    @Test
//    void shouldThrowDuplicateEntityExceptionWhenCreatingClientWithExistingPhone() {
//        // Given
//        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
//                "Другой Иван",
//                "+79123456789", // Дублирующий телефон
//                "ivan2@mail.ru",
//                new PassportCreateDto("5555", "666666")
//        );
//
//        // When & Then
//        assertThatThrownBy(() -> clientService.createClient(createDto))
//                .isInstanceOf(DuplicateEntityException.class)
//                .hasMessageContaining("уже существует");
//    }
//
//    @Test
//    void shouldThrowDuplicateEntityExceptionWhenCreatingClientWithExistingPassport() {
//        // Given
//        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
//                "Другой Иван",
//                "+79123456781",
//                "ivan2@mail.ru",
//                new PassportCreateDto("1234", "567890") // Дублирующий паспорт
//        );
//
//        // When & Then
//        assertThatThrownBy(() -> clientService.createClient(createDto))
//                .isInstanceOf(DuplicateEntityException.class)
//                .hasMessageContaining("уже существует");
//    }
//
//    @Test
//    void shouldGetClientByIdSuccessfully() {
//        // When
//        ClientDoneDto result = clientService.getById(testClient.getId());
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.id()).isEqualTo(testClient.getId());
//        assertThat(result.fullName()).isEqualTo("Иван Иванов");
//        assertThat(result.passport().series()).isEqualTo("1234");
//    }
//
//    @Test
//    void shouldThrowEntityNotFoundExceptionWhenGettingNonExistentClient() {
//        // When & Then
//        assertThatThrownBy(() -> clientService.getById(999999L))
//                .isInstanceOf(EntityNotFoundException.class)
//                .hasMessageContaining("не найден");
//    }
//
//    @Test
//    void shouldGetAllClients() {
//        // Given - создаем еще одного клиента
//        Passport passport2 = new Passport("2222", "333333");
//        Client client2 = new Client("Петр Петров", "+79123456780", "petr@mail.ru", passport2);
//        clientRepository.save(client2);
//
//        // When
//        List<com.example.eventmanagement.dto.ClientDto> clients = clientService.getAll();
//
//        // Then
//        assertThat(clients).hasSize(2);
//        assertThat(clients)
//                .extracting("fullName")
//                .containsExactlyInAnyOrder("Иван Иванов", "Петр Петров");
//    }
//
//    @Test
//    void shouldSearchClientsByName() {
//        // Given - создаем клиента с именем Петр
//        Passport passport2 = new Passport("2222", "333333");
//        Client client2 = new Client("Петр Петров", "+79123456780", "petr@mail.ru", passport2);
//        clientRepository.save(client2);
//
//        // When
//        List<com.example.eventmanagement.dto.ClientDto> result = clientService.searchClients("Петр");
//
//        // Then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).fullName()).isEqualTo("Петр Петров");
//    }
//
//    @Test
//    void shouldSearchClientsByPhone() {
//        // When
//        List<com.example.eventmanagement.dto.ClientDto> result = clientService.searchClients("2345");
//
//        // Then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).fullName()).isEqualTo("Иван Иванов");
//    }
//
//    @Test
//    void shouldDeleteClientWithoutActiveReservations() {
//        // Given - у клиента нет активных бронирований
//
//        // When
//        clientService.deleteClient(testClient.getId());
//
//        // Then
//        assertThat(clientRepository.findById(testClient.getId())).isEmpty();
//        // Паспорт тоже должен удалиться каскадно
//    }
//
//    @Test
//    void shouldThrowBusinessValidationExceptionWhenDeletingClientWithActiveReservations() {
//        // Given - создаем активное бронирование для клиента
//        TicketReservation reservation = new TicketReservation(
//                2,
//                BookingStatus.CONFIRMED,
//                testClient,
//                testEvent
//        );
//        ticketReservationRepository.save(reservation);
//
//        // When & Then
//        assertThatThrownBy(() -> clientService.deleteClient(testClient.getId()))
//                .isInstanceOf(BusinessValidationException.class)
//                .hasMessageContaining("нельзя удалить");
//    }
//
//    @Test
//    void shouldUpdateClientBasicInfoSuccessfully() {
//        // Given
//        com.example.eventmanagement.dto.ClientCreateDto updateDto =
//                new com.example.eventmanagement.dto.ClientCreateDto(
//                        "Иван Обновленный",
//                        "+79123456799",
//                        "ivan.updated@mail.ru"
//                );
//
//        // When
//        ClientDoneDto result = clientService.updateClientBasicInfo(testClient.getId(), updateDto);
//
//        // Then
//        assertThat(result.fullName()).isEqualTo("Иван Обновленный");
//        assertThat(result.phoneNumber()).isEqualTo("+79123456799");
//        assertThat(result.email()).isEqualTo("ivan.updated@mail.ru");
//
//        // Проверяем, что данные обновились в БД
//        Client updatedClient = clientRepository.findById(testClient.getId()).orElseThrow();
//        assertThat(updatedClient.getFullName()).isEqualTo("Иван Обновленный");
//    }
//
//    @Test
//    void shouldThrowDuplicateEntityExceptionWhenUpdatingToExistingEmail() {
//        // Given - создаем второго клиента
//        Passport passport2 = new Passport("2222", "333333");
//        Client client2 = new Client("Петр Петров", "+79123456780", "petr@mail.ru", passport2);
//        clientRepository.save(client2);
//
//        com.example.eventmanagement.dto.ClientCreateDto updateDto =
//                new com.example.eventmanagement.dto.ClientCreateDto(
//                        "Иван Иванов",
//                        "+79123456789",
//                        "petr@mail.ru" // Email уже занят
//                );
//
//        // When & Then
//        assertThatThrownBy(() ->
//                clientService.updateClientBasicInfo(testClient.getId(), updateDto))
//                .isInstanceOf(DuplicateEntityException.class)
//                .hasMessageContaining("уже существует");
//    }
//
//    @Test
//    void shouldReplacePassportSuccessfully() {
//        // Given
//        PassportCreateDto newPassportDto = new PassportCreateDto("9999", "888888");
//
//        // When
//        ClientDoneDto result = clientService.replacePassport(testClient.getId(), newPassportDto);
//
//        // Then
//        assertThat(result.passport().series()).isEqualTo("9999");
//        assertThat(result.passport().number()).isEqualTo("888888");
//
//        // Проверяем, что старый паспорт удалился из БД
//        Client updatedClient = clientRepository.findById(testClient.getId()).orElseThrow();
//        assertThat(updatedClient.getPassport().getSeries()).isEqualTo("9999");
//        assertThat(updatedClient.getPassport().getNumber()).isEqualTo("888888");
//    }
//
//    @Test
//    void shouldThrowDuplicateEntityExceptionWhenReplacingWithExistingPassport() {
//        // Given - создаем второго клиента с другим паспортом
//        Passport passport2 = new Passport("5555", "666666");
//        Client client2 = new Client("Петр Петров", "+79123456780", "petr@mail.ru", passport2);
//        clientRepository.save(client2);
//
//        // Пытаемся заменить паспорт первого клиента на паспорт второго
//        PassportCreateDto duplicatePassportDto = new PassportCreateDto("5555", "666666");
//
//        // When & Then
//        assertThatThrownBy(() ->
//                clientService.replacePassport(testClient.getId(), duplicatePassportDto))
//                .isInstanceOf(DuplicateEntityException.class)
//                .hasMessageContaining("уже существует");
//    }
//
//    @Test
//    void shouldReturnFalseWhenClientHasActiveReservations() {
//        // Given - создаем активное бронирование
//        TicketReservation reservation = new TicketReservation(
//                2,
//                BookingStatus.CONFIRMED,
//                testClient,
//                testEvent
//        );
//        ticketReservationRepository.save(reservation);
//
//        // When
//        boolean canDelete = clientService.canDeleteClient(testClient.getId());
//
//        // Then
//        assertThat(canDelete).isFalse();
//    }
//
//    @Test
//    void shouldReturnTrueWhenClientHasNoActiveReservations() {
//        // Given - у клиента нет бронирований
//
//        // When
//        boolean canDelete = clientService.canDeleteClient(testClient.getId());
//
//        // Then
//        assertThat(canDelete).isTrue();
//    }
//
//    @Test
//    void shouldReturnTrueWhenClientHasOnlyCanceledReservations() {
//        // Given - создаем отмененное бронирование
//        TicketReservation reservation = new TicketReservation(
//                2,
//                BookingStatus.CANCELED,
//                testClient,
//                testEvent
//        );
//        ticketReservationRepository.save(reservation);
//
//        // When
//        boolean canDelete = clientService.canDeleteClient(testClient.getId());
//
//        // Then
//        assertThat(canDelete).isTrue();
//    }
//}