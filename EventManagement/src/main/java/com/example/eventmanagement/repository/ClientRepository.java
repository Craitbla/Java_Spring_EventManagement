package com.example.eventmanagement.repository;
import com.example.eventmanagement.dto.PassportCreateDto;
import com.example.eventmanagement.dto.PassportDto;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.security.auth.spi.LoginModule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByFullNameIgnoreCase(String fullName);
    List<Client> findByFullNameContainingIgnoreCase(String fullNamePart);
    Optional<Client> findByPhoneNumber(String phoneNumber);
    Optional<Client> findByEmail(String email);
    Optional<Client> findByPassport(Passport passport);
    List<Client> findByCreatedAt(LocalDateTime createdAt);
    List<Client> findByCreatedAtBefore(LocalDateTime date);
    List<Client> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Client> findByCreatedAtAfter(LocalDateTime date);
    List<Client> findByUpdatedAt(LocalDateTime updatedAt);
    List<Client> findByUpdatedAtBefore(LocalDateTime date);
    List<Client> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Client> findByUpdatedAtAfter(LocalDateTime date);
    @Query("SELECT c FROM Client c JOIN FETCH c.passport WHERE c.id = :id")
    Optional<Client> findByIdWithPassport(@Param("id") Long id);

    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.ticketReservations WHERE c.id = :id")
    Optional<Client> findByIdWithTicketReservations(@Param("id") Long id);

    @Query("SELECT c FROM Client c WHERE " +
           "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "c.phoneNumber LIKE CONCAT('%', :search, '%') OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Client> searchClients(@Param("search") String search);






    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);



}