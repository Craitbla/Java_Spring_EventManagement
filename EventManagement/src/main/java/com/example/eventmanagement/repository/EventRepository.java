package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findById(Long id);
    List<Event> findByName(String name);
    List<Event> findByDate(LocalDate date);
    List<Event> findByTicketPrice(BigDecimal ticketPrice);
    List<Event> findByStatus(EventStatus status);
    List<Event> findByDescription(String description);
    List<Event> findByCreatedAt(LocalDateTime createdAt);
    List<Event> findByUpdatedAt(LocalDateTime updatedAt);

    //пока без связных частей по  @OneToMany и тд
}
