package com.example.eventmanagement.service;

import com.example.eventmanagement.repository.EventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class EventService {
    private  final EventRepository eventRepository;
    EventService(EventRepository eventRepository){
        this.eventRepository = eventRepository;
    }
}
