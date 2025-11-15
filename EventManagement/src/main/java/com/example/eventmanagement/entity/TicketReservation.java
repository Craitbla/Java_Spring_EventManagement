package com.example.eventmanagement.entity;

import com.example.eventmanagement.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "ticket_reservations")
public class TicketReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JsonIgnoreProperties({"ticketReservation", "passport"})
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @ManyToOne
    @JsonIgnoreProperties({"ticketReservation"})
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    @Column(name = "number_of_tickets", nullable = false)
    @Min(value = 1, message = "Количество билетов должно быть больше или равно 1")
    private Integer numberOfTickets;
    @Column(name = "booking_status", nullable = false)
    private BookingStatus bookingStatus;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public TicketReservation() {

    }

    @PrePersist
    protected void onCreate() {
        if (numberOfTickets == null) {
            numberOfTickets = 1;
        }
        if (bookingStatus == null) {
            bookingStatus = BookingStatus.PENDING_CONFIRMATION;
        }
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

    public TicketReservation(Integer numberOfTickets, BookingStatus bookingStatus) {
        this.numberOfTickets = numberOfTickets;
        this.bookingStatus = bookingStatus;
    }

    public static TicketReservation createForTesting(Integer numberOfTickets, BookingStatus bookingStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
        TicketReservation reservation = new TicketReservation(numberOfTickets, bookingStatus);
        reservation.setCreatedAt(createdAt);
        reservation.setUpdatedAt(updatedAt);
        return reservation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    void assignClient(Client client) {
        this.client = client;
    }

    public Event getEvent() {
        return event;
    }

    void assignEvent(Event event) {

        this.event = event;
    }

    public Integer getNumberOfTickets() {
        return numberOfTickets;
    }

    public void setNumberOfTickets(Integer numberOfTickets) {
        this.numberOfTickets = numberOfTickets;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketReservation that = (TicketReservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TicketReservation{" +
               "id=" + id +
               ", numberOfTickets=" + numberOfTickets +
               ", bookingStatus='" + bookingStatus + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}
