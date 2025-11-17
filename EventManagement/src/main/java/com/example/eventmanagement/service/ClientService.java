package com.example.eventmanagement.service;

import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.PassportRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ClientService {
    private final ClientRepository clientRepository;
    private final PassportRepository passportRepository;

    ClientService(ClientRepository clientRepository, PassportRepository passportRepository) {
        this.clientRepository = clientRepository;
        this.passportRepository = passportRepository;
    }

}

