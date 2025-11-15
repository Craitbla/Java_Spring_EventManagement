package com.example.eventmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "clients", uniqueConstraints = {
        @UniqueConstraint(columnNames = "phone_number"),
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "passport_id")
})
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "full_name", nullable = false)
    @NotBlank(message = "ФИО не может быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно иметь длину между 2 и 100 буквами")
    private String fullName;
    @Column(name = "phone_number", nullable = false)
    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\+7\\d{10}$", message = "Телефон должен начинаться с +7 и иметь 11 цифр")
    private String phoneNumber;
    @Column
    @Email(message = "Email должен быть валидным") //есть два варианта из разных библиотек
    private String email;

    @NotNull(message = "Паспорт обязателен")
    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnoreProperties("client")
    @JoinColumn(name = "passport_id", nullable = false)
    private Passport passport;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("client")
    private List<TicketReservation> ticketReservations = new ArrayList<>();


    public Client() {

    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }

    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Client(String fullName, String phoneNumber, String email, Passport passport) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.passport = passport;
    }

    public Client(String fullName, String phoneNumber, Passport passport) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.passport = passport;
    }
    public static Client createForTesting(String fullName, String phoneNumber, String email, Passport passport, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Client client = new Client(fullName, phoneNumber, email, passport);
        client.setCreatedAt(createdAt);
        client.setUpdatedAt(updatedAt);
        return client;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Passport getPassport() {//////
        return passport;
    }

    public void setPassport(Passport passport) {
        if (this.getPassport() != null) {
            this.getPassport().assignClient(null);
        }
        this.passport = passport;
        if (passport != null) {
            passport.assignClient(this);
        }

    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private void setCreatedAt(LocalDateTime time) {
        createdAt = time;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TicketReservation> getTicketReservations() {
        return ticketReservations;
    }

    public void setTicketReservations(List<TicketReservation> newReservations) {
        if (this.ticketReservations != null) {
            for (TicketReservation oldReservation : this.ticketReservations) {
                if (oldReservation != null && this.equals(oldReservation.getClient())) {
                    oldReservation.assignClient(null);
                }
            }
        }

        this.ticketReservations = newReservations != null ?
                new ArrayList<>(newReservations) : new ArrayList<>();

        for (TicketReservation newReservation : this.ticketReservations) {
            if (newReservation != null) {
                newReservation.assignClient(this);
            }
        }

    }

    public void addTicketReservation(TicketReservation ticketReservation) {
        ticketReservation.assignClient(this);
        this.ticketReservations.add(ticketReservation);
    }

    public boolean removeTicketReservation(TicketReservation reservation) {
        boolean removed = this.ticketReservations.remove(reservation);
        if (removed) {
            reservation.assignClient(null);
        }
        return removed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}