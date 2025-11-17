package com.example.eventmanagement.service;

import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.PassportRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PassportService {
    private  final PassportRepository passportRepository;
    PassportService(PassportRepository passportRepository){
        this.passportRepository = passportRepository;
    }
}
