package com.example.eventmanagement.repository;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketReservationRepository extends JpaRepository<TicketReservation, Long> {
    Optional<TicketReservation> findById(Long id);
    List<TicketReservation> findByNumberOfTickets(Integer numberOfTickets);
    List<TicketReservation> findByClientId(Long clientId);
    List<TicketReservation> findByEventId(Long eventId);
    List<TicketReservation> findByNumberOfTicketsLessThan(Integer numberOfTickets);
    List<TicketReservation> findByNumberOfTicketsBetween(Integer start, Integer end);
    List<TicketReservation> findByNumberOfTicketsGreaterThan(Integer numberOfTickets);
    List<TicketReservation> findByBookingStatus(BookingStatus bookingStatus);
    List<TicketReservation> findByBookingStatusIn(List<BookingStatus> bookingStatus);

    List<TicketReservation> findByCreatedAt(LocalDateTime createdAt); //
    List<TicketReservation> findByCreatedAtBefore(LocalDateTime date);
    List<TicketReservation> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<TicketReservation> findByCreatedAtAfter(LocalDateTime date);
    List<TicketReservation> findByUpdatedAt(LocalDateTime updatedAt);
    List<TicketReservation> findByUpdatedAtBefore(LocalDateTime date);
    List<TicketReservation> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<TicketReservation> findByUpdatedAtAfter(LocalDateTime date);
    List<TicketReservation> findByClientIdAndEventIdAndBookingStatus(
            Long clientId, Long eventId, BookingStatus status);

    @Query("SELECT tr FROM TicketReservation tr JOIN FETCH tr.client WHERE tr.id = :id")
    List<Client> findByIdWithClient(@Param("id") Long id);
    @Query("SELECT tr FROM TicketReservation tr JOIN FETCH tr.event WHERE tr.id = :id")
    List<Client> findByIdWithEvent(@Param("id") Long id);

    @Query("SELECT tr FROM TicketReservation tr JOIN FETCH tr.client JOIN FETCH tr.event WHERE tr.id = :id")
    List<Client> findByIdWithClientAndEvent(@Param("id") Long id); //не проверено

}
