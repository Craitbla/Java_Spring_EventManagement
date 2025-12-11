package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.exception.BusinessValidationException;
import com.example.eventmanagement.exception.DuplicateEntityException;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.mapper.ClientMapper;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.PassportRepository;
import com.example.eventmanagement.repository.TicketReservationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j
public class ClientService {
    private final ClientRepository clientRepository;
    private final PassportRepository passportRepository;
    private final TicketReservationRepository ticketReservationRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, PassportRepository passportRepository, TicketReservationRepository ticketReservationRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.passportRepository = passportRepository;
        this.ticketReservationRepository = ticketReservationRepository;
        this.clientMapper = clientMapper;
    }

    public List<ClientDoneDto> getAll() {
        log.debug("Получение списка всех клиентов");
        return clientMapper.toClientDoneDtoList(clientRepository.findAll());
    }

    public ClientDoneDto getById(Long id) {
        log.debug("Получение клиента по ID: {}", id);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Клиент с id %d не найден", id)
                ));
        return clientMapper.toClientDoneDto(client);
    }

    public ClientDoneDto createClient(ClientCreateWithDependenciesDto dto) {
        log.info("Создание клиента: {}", dto.email());

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
        log.info("Клиент создан с ID: {}", savedClient.getId());
        return clientMapper.toClientDoneDto(savedClient);
    }

    public void deleteClient(Long id) {
        log.info("Удаление клиента с ID: {}", id);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Клиент с id %d не найден", id)
                ));
        if(!canDeleteClient(id)){
            throw new BusinessValidationException(String.format("Этого клиента с id %d нельзя удалить, у него еще есть активные бронирования", id));
        }
        clientRepository.delete(client);
        log.info("Клиент с ID {} удален", id);
    }

    public List<ClientDoneDto> searchClients(String searchTerm) {
        log.debug("Поиск клиентов по запросу: {}", searchTerm);
        List<Client> foundedClients = clientRepository.searchClients(searchTerm);
        return clientMapper.toClientDoneDtoList(foundedClients);
    }

    public boolean canDeleteClient(Long clientId) {
        log.debug("Проверка возможности удаления клиента с ID: {}", clientId);
        List<BookingStatus> bookingStatusList = new ArrayList<>();
        bookingStatusList.add(BookingStatus.CONFIRMED);
        bookingStatusList.add(BookingStatus.PENDING_CONFIRMATION);
        return ticketReservationRepository.findByClientIdAndBookingStatusIn(clientId, bookingStatusList).isEmpty();
    }

    public ClientDoneDto updateClientBasicInfo(Long id, ClientCreateDto dto) {
        log.info("Обновление базовой информации клиента с ID: {}", id);
        Client client = clientRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Клиент с id %d не найден", id))
        );
        if (clientRepository.existsByPhoneNumberAndIdNot(dto.phoneNumber(), id)) {
            throw new DuplicateEntityException("Клиент c таким телефоном " + dto.phoneNumber() + " уже существует");
        }
        client.setFullName(dto.fullName());
        client.setPhoneNumber(dto.phoneNumber());
        client.setEmail(dto.email());

        Client updatedClient = clientRepository.save(client);
        log.info("Базовая информация клиента с ID {} обновлена", id);
        return clientMapper.toClientDoneDto(updatedClient);
    }

    public ClientDoneDto replacePassport(Long clientId, PassportCreateDto newPassportDto){
        log.info("Замена паспорта для клиента с ID: {}", clientId);
        Client client = clientRepository.findById(clientId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Клиент с id %d не найден", clientId))
        );
        Passport oldPassport = client.getPassport();
        if (passportRepository.existsBySeriesAndNumber(
                newPassportDto.series(), newPassportDto.number())) {
            throw new DuplicateEntityException("Паспорт " +newPassportDto.series()+" "+ newPassportDto.number()+ " уже существует");
        }
        Passport newPassport = new Passport(newPassportDto.series(), newPassportDto.number());

        client.setPassport(newPassport);
        Client updatedClient = clientRepository.save(client);

        if(oldPassport!=null){
            passportRepository.delete(oldPassport);
            log.debug("Старый паспорт удален");
        }

        log.info("Паспорт для клиента с ID {} заменен", clientId);
        return clientMapper.toClientDoneDto(updatedClient);
    }
}