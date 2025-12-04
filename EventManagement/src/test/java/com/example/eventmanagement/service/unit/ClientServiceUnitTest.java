package com.example.eventmanagement.service.unit;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.exception.BusinessValidationException;
import com.example.eventmanagement.exception.DuplicateEntityException;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.exception.OperationNotAllowedException;
import com.example.eventmanagement.mapper.ClientMapper;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.PassportRepository;
import com.example.eventmanagement.repository.TicketReservationRepository;
import com.example.eventmanagement.service.ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceUnitTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PassportRepository passportRepository;

    @Mock
    private TicketReservationRepository ticketReservationRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    @Test
    void createClient_Success() {
        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );
        Client client = new Client("Иван Иванов", "+79123456789", "ivan@mail.ru",
                new Passport("1234", "567890"));
        ClientDoneDto expectedDto = new ClientDoneDto(1L, "Иван Иванов", "+79123456789",
                "ivan@mail.ru", new PassportCreateDto("1234", "567890"),
                LocalDateTime.now(), LocalDateTime.now());

        when(clientRepository.existsByEmail("ivan@mail.ru")).thenReturn(false);
        when(clientRepository.existsByPhoneNumber("+79123456789")).thenReturn(false);
        when(passportRepository.existsBySeriesAndNumber("1234", "567890")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toClientDoneDto(client)).thenReturn(expectedDto);

        ClientDoneDto result = clientService.createClient(createDto);

        assertNotNull(result);
        assertEquals("Иван Иванов", result.fullName());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void createClient_DuplicateEmail_ThrowsException() {
        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );

        when(clientRepository.existsByEmail("ivan@mail.ru")).thenReturn(true);

        assertThrows(DuplicateEntityException.class, () -> clientService.createClient(createDto));
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void createClient_DuplicatePhone_ThrowsException() {
        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );

        when(clientRepository.existsByEmail("ivan@mail.ru")).thenReturn(false);
        when(clientRepository.existsByPhoneNumber("+79123456789")).thenReturn(true);

        assertThrows(DuplicateEntityException.class, () -> clientService.createClient(createDto));
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void createClient_DuplicatePassport_ThrowsException() {
        ClientCreateWithDependenciesDto createDto = new ClientCreateWithDependenciesDto(
                "Иван Иванов", "+79123456789", "ivan@mail.ru",
                new PassportCreateDto("1234", "567890")
        );

        when(clientRepository.existsByEmail("ivan@mail.ru")).thenReturn(false);
        when(clientRepository.existsByPhoneNumber("+79123456789")).thenReturn(false);
        when(passportRepository.existsBySeriesAndNumber("1234", "567890")).thenReturn(true);

        assertThrows(DuplicateEntityException.class, () -> clientService.createClient(createDto));
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void deleteClient_Success() {
        Client client = new Client("Иван Иванов", "+79123456789", "ivan@mail.ru",
                new Passport("1234", "567890"));
        client.setId(1L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(ticketReservationRepository.findByClientIdAndBookingStatusIn(eq(1L), any()))
                .thenReturn(List.of());

        clientService.deleteClient(1L);

        verify(clientRepository).delete(client);
    }

    @Test
    void deleteClient_NotFound_ThrowsException() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clientService.deleteClient(1L));
        verify(clientRepository, never()).delete(any());
    }

    @Test
    void deleteClient_WithActiveReservations_ThrowsException() {
        Client client = new Client("Иван Иванов", "+79123456789", "ivan@mail.ru",
                new Passport("1234", "567890"));
        client.setId(1L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(ticketReservationRepository.findByClientIdAndBookingStatusIn(eq(1L), any()))
                .thenReturn(List.of(mock(com.example.eventmanagement.entity.TicketReservation.class)));

        assertThrows(BusinessValidationException.class, () -> clientService.deleteClient(1L));
        verify(clientRepository, never()).delete(any());
    }

    @Test
    void updateClientBasicInfo_Success() {
        Client client = new Client("Иван Иванов", "+79123456789", "ivan@mail.ru",
                new Passport("1234", "567890"));
        client.setId(1L);
        ClientCreateDto updateDto = new ClientCreateDto("Петр Петров", "+79123456780", "petr@mail.ru");
        ClientDoneDto expectedDto = new ClientDoneDto(1L, "Петр Петров", "+79123456780",
                "petr@mail.ru", new PassportCreateDto("1234", "567890"),
                LocalDateTime.now(), LocalDateTime.now());

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.existsByEmailAndIdNot("petr@mail.ru", 1L)).thenReturn(false);
        when(clientRepository.existsByPhoneNumberAndIdNot("+79123456780", 1L)).thenReturn(false);
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toClientDoneDto(client)).thenReturn(expectedDto);

        ClientDoneDto result = clientService.updateClientBasicInfo(1L, updateDto);

        assertNotNull(result);
        assertEquals("Петр Петров", result.fullName());
        verify(clientRepository).save(client);
    }

    @Test
    void replacePassport_Success() {
        Client client = new Client("Иван Иванов", "+79123456789", "ivan@mail.ru",
                new Passport("1234", "567890"));
        client.setId(1L);
        PassportCreateDto newPassportDto = new PassportCreateDto("4321", "098765");
        ClientDoneDto expectedDto = new ClientDoneDto(1L, "Иван Иванов", "+79123456789",
                "ivan@mail.ru", new PassportCreateDto("4321", "098765"),
                LocalDateTime.now(), LocalDateTime.now());

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(passportRepository.existsBySeriesAndNumber("4321", "098765")).thenReturn(false);
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toClientDoneDto(client)).thenReturn(expectedDto);

        ClientDoneDto result = clientService.replacePassport(1L, newPassportDto);

        assertNotNull(result);
        verify(clientRepository).save(client);
        verify(passportRepository).delete(any(Passport.class));
    }

    @Test
    void canDeleteClient_NoActiveReservations_ReturnsTrue() {
        when(ticketReservationRepository.findByClientIdAndBookingStatusIn(eq(1L), any()))
                .thenReturn(List.of());

        boolean result = clientService.canDeleteClient(1L);

        assertTrue(result);
    }

    @Test
    void canDeleteClient_WithActiveReservations_ReturnsFalse() {
        when(ticketReservationRepository.findByClientIdAndBookingStatusIn(eq(1L), any()))
                .thenReturn(List.of(mock(com.example.eventmanagement.entity.TicketReservation.class)));

        boolean result = clientService.canDeleteClient(1L);

        assertFalse(result);
    }

    @Test
    void searchClients_Success() {
        List<Client> clients = List.of(new Client("Иван Иванов", "+79123456789", "ivan@mail.ru",
                new Passport("1234", "567890")));
        List<ClientDto> expectedDtos = List.of(new ClientDto(1L, "Иван Иванов", "+79123456789", "ivan@mail.ru"));

        when(clientRepository.searchClients("Иван")).thenReturn(clients);
        when(clientMapper.toClientDtoList(clients)).thenReturn(expectedDtos);

        List<ClientDto> result = clientService.searchClients("Иван");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(clientRepository).searchClients("Иван");
    }
}