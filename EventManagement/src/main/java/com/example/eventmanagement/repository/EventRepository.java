package com.example.eventmanagement.repository;

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
    Optional<Event> findById(Long id);
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

    @Query("SELECT e FROM Event e JOIN FETCH TicketReservation WHERE e.id = :id")
    List<Event> findByIdTicketReservations(@Param("id") Long id);
    boolean existsByNameAndDate(String name, LocalDate date);

    //пока без связных частей по  @OneToMany и тд
}
