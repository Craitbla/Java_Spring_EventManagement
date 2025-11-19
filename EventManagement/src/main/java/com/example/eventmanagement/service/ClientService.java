package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.exception.DuplicateEntityException;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.mapper.ClientMapper;
import com.example.eventmanagement.mapper.PassportMapper;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.PassportRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ClientService {
    private final ClientRepository clientRepository;
    private final PassportRepository passportRepository;
    private final ClientMapper clientMapper;

    ClientService(ClientRepository clientRepository, PassportRepository passportRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.passportRepository = passportRepository;
        this.clientMapper = clientMapper;
    }

    // 1. Валидация при создании
    public ClientDoneDto createClient(ClientCreateWithDependenciesDto dto) {
        //без связей, все связи в сервисе
//        Client client = clientMapper.fromCreateWithoutDependenciesDto(dto);

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
//        // Проверить уникальность телефона, email, паспорта
//        // Создать и сохранить клиента с паспортом
//        // Вернуть DTO
        return clientMapper.toClientDoneDto(savedClient);
    }
//
//    // 2. Каскадные операции
    public void deleteClient(Long id) {

        if(!clientRepository.existsById(id)){
            throw new EntityNotFoundException("Клиент с id " + id + " не найден");
        }
        Client client = clientRepository.findById(id).get();
        clientRepository.delete(client);

//        // Найти клиента
//        // Удалить все его бронирования (каскадно)
//        // Удалить клиента (паспорт удалится каскадно)
    }
//
//    // 3. Бизнес-логика поиска
//    public List<ClientDto> searchClients(String searchTerm) {
//        // Использовать searchClients из репозитория
//        // Преобразовать в DTO
//    }
//
//    // 4. Проверка зависимостей перед удалением
//    public boolean canDeleteClient(Long clientId) {
//        // Проверить, есть ли активные бронирования
//        // Вернуть true/false в зависимости от бизнес-правил
//    }

}

