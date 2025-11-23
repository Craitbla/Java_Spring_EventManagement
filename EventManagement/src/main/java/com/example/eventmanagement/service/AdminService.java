package com.example.eventmanagement.service;

import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.repository.TicketReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AdminService {

    private final TicketReservationRepository ticketReservationRepository;

    public AdminService(TicketReservationRepository ticketReservationRepository) {
        this.ticketReservationRepository = ticketReservationRepository;
    }

    @Transactional
    public int cleanupOldCanceledReservations() {
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        List<TicketReservation> oldCanceled = ticketReservationRepository
                .findByBookingStatusAndUpdatedAtBefore(BookingStatus.CANCELED, monthAgo);

        if (!oldCanceled.isEmpty()) {
            log.info("Очистка {} старых отмененных бронирований", oldCanceled.size());
            ticketReservationRepository.deleteAll(oldCanceled);
        }
        return oldCanceled.size();
    }
}