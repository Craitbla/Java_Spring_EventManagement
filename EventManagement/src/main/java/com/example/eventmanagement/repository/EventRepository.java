package com.example.eventmanagement.repository;

import com.example.eventmanagement.dto.EventStatisticsDto;
import com.example.eventmanagement.dto.EventWithReservationCountDto;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByNameIgnoreCase(String name);
    List<Event> findByNameContainingIgnoreCase(String namePart);
    List<Event> findByDate(LocalDate date);
    Optional<Event> findByNameAndDate(String name, LocalDate date);
    List<Event> findByTicketPrice(BigDecimal ticketPrice);
    List<Event> findByTicketPriceLessThan(BigDecimal ticketPrice);
    List<Event> findByTicketPriceBetween(BigDecimal minPrice,BigDecimal maxPrice);
    List<Event> findByStatus(EventStatus status);
    List<Event> findByStatusIn(List<EventStatus> status);
    List<Event> findByDescription(String description);
    List<Event> findByCreatedAt(LocalDateTime createdAt); //
    List<Event> findByCreatedAtBefore(LocalDateTime date);
    List<Event> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Event> findByCreatedAtAfter(LocalDateTime date);
    List<Event> findByUpdatedAt(LocalDateTime updatedAt);
    List<Event> findByUpdatedAtBefore(LocalDateTime date);
    List<Event> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Event> findByUpdatedAtAfter(LocalDateTime date);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.ticketReservations WHERE e.id = :id")
    Optional<Event> findByIdWithTicketReservations(@Param("id") Long id);
    boolean existsByNameAndDate(String name, LocalDate date);

    @Query("SELECT COALESCE(SUM(tr.numberOfTickets), 0L) " +
           "FROM TicketReservation tr " +
           "WHERE tr.event.id = :eventId AND tr.bookingStatus = com.example.eventmanagement.enums.BookingStatus.CONFIRMED")
    Long countConfirmedTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT e, COUNT(t) as reservationCount " +
           "FROM Event e LEFT JOIN e.ticketReservations t " +
           "WHERE e.id = :eventId " +
           "GROUP BY e")
    Optional<EventWithReservationCountDto> findByIdWithReservationCount(@Param("eventId") Long eventId);

    //// В EventRepository
    //Optional<Event> findById(Long eventId);
    //
    //@Query("SELECT COALESCE(SUM(tr.numberOfTickets), 0L) " +
    //       "FROM TicketReservation tr " +
    //       "WHERE tr.event.id = :eventId AND tr.bookingStatus = com.example.eventmanagement.enums.BookingStatus.CONFIRMED")
    //Long countConfirmedTicketsByEventId(@Param("eventId") Long eventId);
    //
    //// В сервисе
    //public EventStatisticsDto getEventStatistics(Long eventId) {
    //    Event event = eventRepository.findById(eventId)
    //        .orElseThrow(() -> new EventNotFoundException(eventId));
    //
    //    Long confirmedTickets = eventRepository.countConfirmedTicketsByEventId(eventId);
    //    BigDecimal totalRevenue = event.getTicketPrice().multiply(BigDecimal.valueOf(confirmedTickets));
    //
    //    return new EventStatisticsDto(
    //        event.getId(),
    //        event.getName(),
    //        event.getDate(),
    //        event.getStatus(),
    //        confirmedTickets,
    //        event.getTicketPrice(),
    //        totalRevenue
    //    );
    //}
}
