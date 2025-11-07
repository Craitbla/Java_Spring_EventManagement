package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PassportRepository extends JpaRepository<Passport, Long> {
    Optional<Passport> findBySeriesAndNumber(String series, String number);
    List<Passport> findByCreatedAt(LocalDateTime createdAt); //
    List<Passport> findByCreatedAtBefore(LocalDateTime date);
    List<Passport> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Passport> findByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT p FROM Passport p JOIN FETCH p.client WHERE p.id = :id")
    Optional<Passport> findByIdWithClient(@Param("id") Long id);
    boolean existsBySeriesAndNumber(String series, String number);


}
