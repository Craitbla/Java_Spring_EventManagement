package com.example.eventmanagement.service;

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
//    public ClientDto createClient(ClientDto clientDto) {
//        // Проверить уникальность телефона, email, паспорта
//        // Создать и сохранить клиента с паспортом
//        // Вернуть DTO
//    }
//
//    // 2. Каскадные операции
//    public void deleteClient(Long id) {
//        // Найти клиента
//        // Удалить все его бронирования (каскадно)
//        // Удалить клиента (паспорт удалится каскадно)
//    }
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

