package com.example.eventmanagement.repository;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import org.springframework.data.jpa.repository.JpaRepository;

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
    List<Client> findByFullName(String fullName);
    List<Client> findByPhoneNumber(String phoneNumber);
    List<Client> findByEmail(String email);
    List<Client> findByPassport(Passport passport);
    List<Client> findByCreatedAt(LocalDateTime createdAt);
    List<Client> findByUpdatedAt(LocalDateTime updatedAt);

    boolean existsByFullName(String fullName);

}