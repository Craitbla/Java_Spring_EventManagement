package com.example.eventmanagement.repository;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
//    Волшебство Spring Data JPA:
//    вам не нужно писать реализацию этих методов!
//    Spring сам сгенерирует SQL-запросы на основе названий методов.
    Optional<Client> findById(Long id);
//2 вар Optional
//везде листа потому что все может быть не уникальным yesss
    List<Client> findByFullNameIgnoreCase(String fullName);
    List<Client> findByFullNameContainingIgnoreCase(String fullNamePart);
    Optional<Client> findByPhoneNumber(String phoneNumber);
    Optional<Client> findByEmail(String email);
    List<Client> findByPassport(Passport passport);
    List<Client> findByCreatedAt(LocalDateTime createdAt); //
    List<Client> findByCreatedAtBefore(LocalDateTime date);
    List<Client> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Client> findByCreatedAtAfter(LocalDateTime date);
    List<Client> findByUpdatedAt(LocalDateTime updatedAt);
    List<Client> findByUpdatedAtBefore(LocalDateTime date);
    List<Client> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Client> findByUpdatedAtAfter(LocalDateTime date);

    //Абстракция над SQL - оперирует сущностями и их полями, а не таблицами
    @Query("SELECT c FROM Client c JOIN FETCH c.passport WHERE c.id = :id")
    List<Client> findByIdWithPassport(@Param("id") Long id);

    @Query("SELECT c FROM Client c WHERE " +
           "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "c.phoneNumber LIKE CONCAT('%', :search, '%') OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Client> searchClients(@Param("id") String search);

    @Query("SELECT c FROM Client c JOIN FETCH c.ticketReservations WHERE c.id = :id")
    List<Client> findByIdWithTicketReservations(@Param("id") Long id);



    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);


}