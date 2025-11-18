package com.example.eventmanagement.service;

import com.example.eventmanagement.mapper.ClientMapper;
import com.example.eventmanagement.mapper.EventMapper;
import com.example.eventmanagement.repository.EventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class EventService {
    private  final EventRepository eventRepository;
    private final EventMapper eventMapper;
    EventService(EventRepository eventRepository, EventMapper eventMapper){
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }
}
