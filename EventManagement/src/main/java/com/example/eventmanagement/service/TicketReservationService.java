package com.example.eventmanagement.service;

import com.example.eventmanagement.mapper.EventMapper;
import com.example.eventmanagement.mapper.TicketReservationMapper;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.PassportRepository;
import com.example.eventmanagement.repository.TicketReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TicketReservationService {
    private  final TicketReservationRepository ticketReservationRepository;
    private  final ClientRepository clientRepository;
    private  final EventRepository eventRepository;
    private final TicketReservationMapper ticketReservationMapper;
    public TicketReservationService(TicketReservationRepository ticketReservationRepository, ClientRepository clientRepository, EventRepository eventRepository, TicketReservationMapper ticketReservationMapper) {
        this.ticketReservationRepository = ticketReservationRepository;
        this.clientRepository = clientRepository;
        this.eventRepository = eventRepository;
        this.ticketReservationMapper = ticketReservationMapper;
    }
}
