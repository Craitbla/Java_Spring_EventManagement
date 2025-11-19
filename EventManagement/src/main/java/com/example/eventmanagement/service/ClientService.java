package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.exception.DuplicateEntityException;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.exception.OperationNotAllowedException;
import com.example.eventmanagement.mapper.ClientMapper;
import com.example.eventmanagement.mapper.PassportMapper;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.PassportRepository;
import com.example.eventmanagement.repository.TicketReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ClientService {
    private final ClientRepository clientRepository;
    private final PassportRepository passportRepository;
    private final TicketReservationRepository ticketReservationRepository;
    private final ClientMapper clientMapper;
    private final PassportService passportService;

    public ClientService(ClientRepository clientRepository, PassportRepository passportRepository, TicketReservationRepository ticketReservationRepository, ClientMapper clientMapper, PassportService passportService) {
        this.clientRepository = clientRepository;
        this.passportRepository = passportRepository;
        this.ticketReservationRepository = ticketReservationRepository;
        this.clientMapper = clientMapper;
        this.passportService = passportService;
    }

    public ClientDoneDto createClient(ClientCreateWithDependenciesDto dto) {

        if (clientRepository.existsByEmail(dto.email())) {
            throw new DuplicateEntityException("Клиент c таким email " + dto.email() + " уже существует");
        }
        if (clientRepository.existsByPhoneNumber(dto.phoneNumber())) {
            throw new DuplicateEntityException("Клиент c таким телефоном " + dto.phoneNumber() + " уже существует");
        }
        PassportCreateDto passportDto = dto.passport();
        if (passportRepository.existsBySeriesAndNumber(passportDto.series(), passportDto.number())) {
            throw new DuplicateEntityException("Клиент c таким паспортом " + passportDto.number() + passportDto.series() + " уже существует");
        }
        Client client = new Client(
                dto.fullName(),
                dto.phoneNumber(),
                dto.email(),
                new Passport(passportDto.series(), passportDto.number())
        );
        Client savedClient = clientRepository.save(client);
        return clientMapper.toClientDoneDto(savedClient);
    }

    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Клиент с id %d не найден", id)
                ));
        clientRepository.delete(client);

    }
    public List<ClientDto> searchClients(String searchTerm) {
        List<Client> foundedClients = clientRepository.searchClients(searchTerm);
        return clientMapper.toClientDtoList(foundedClients);
    }
    public boolean canDeleteClient(Long clientId) {
        List<BookingStatus> bookingStatusList = new ArrayList<>();
        bookingStatusList.add(BookingStatus.CONFIRMED);
        bookingStatusList.add(BookingStatus.PENDING_CONFIRMATION);
        if(ticketReservationRepository.findByClientIdAndBookingStatusIn(clientId, bookingStatusList).isEmpty()){
            return true;
        }
        return false;
    }
    public ClientDoneDto updateClientBasicInfo(Long id, ClientCreateWithDependenciesDto dto) { //пока один
        Client client = clientRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Клиент с id %d не найден", id))
        );
        if (clientRepository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new OperationNotAllowedException("Клиент c таким email " + dto.email() + " уже существует");
        }
        if (clientRepository.existsByPhoneNumberAndIdNot(dto.phoneNumber(), id)) {
            throw new OperationNotAllowedException("Клиент c таким телефоном " + dto.phoneNumber() + " уже существует");
        }
        client.setFullName(dto.fullName());
        client.setPhoneNumber(dto.phoneNumber());
        client.setEmail(dto.email());

        PassportCreateDto passportDto = dto.passport();
        if (passportDto != null) {
            if (passportRepository.existsBySeriesAndNumberAndIdNot(passportDto.series(), passportDto.number(), id)) {
                throw new OperationNotAllowedException("Клиент c таким паспортом " + passportDto.number() + passportDto.series() + " уже существует");
            }
            Passport foundedPassport = passportRepository.findBySeriesAndNumber(passportDto.series(), passportDto.number()).orElseThrow(
                    () -> new EntityNotFoundException("Паспорт клиента, которого хотят обновить не найден, id клиента: " + id)
            );
            passportService.deletePassport(foundedPassport.getId());
            Passport newPassport = passportService.createPassport(passportDto);
            client.setPassport(newPassport);

        }


        //проверить существует ли    ок
        //проверить такой же ли, если другой то
        //удалить старый, похуй, сделаю через сервис паспорта
        Client updatedClient = clientRepository.save(client);
        return clientMapper.toClientDoneDto(updatedClient);
        //насколько я понимаю flush потом в контроллере
    }

}

