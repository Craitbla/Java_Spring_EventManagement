package com.example.eventmanagement.repository;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketReservationRepository extends JpaRepository<TicketReservation, Long> {
    Optional<TicketReservation> findById(Long id);
    List<TicketReservation> findByNumberOfTickets(Integer numberOfTickets);
    List<TicketReservation> findByBookingStatus(BookingStatus bookingStatus);
    List<TicketReservation> findByCreatedAt(LocalDateTime createdAt);
    List<TicketReservation> findByUpdatedAt(LocalDateTime updatedAt);
}
