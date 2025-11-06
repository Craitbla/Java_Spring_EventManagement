package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.Passport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PassportRepository extends JpaRepository<Passport, Long> {
    Optional<Passport> findById(Long id);
    Optional<Passport> findBySeriesAndNumber(String series, String number);
    List<Passport> findByCreatedAt(LocalDateTime createdAt);
}
