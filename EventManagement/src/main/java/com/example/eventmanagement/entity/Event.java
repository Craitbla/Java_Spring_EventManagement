package com.example.eventmanagement.entity;

import com.example.eventmanagement.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "events", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "date"})})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @NotBlank(message = "Название не может быть пустым")
    private String name;
    @Column(nullable = false)
    @Future(message = "Дата должна быть будущей")
    private LocalDate date;
    @Column(name = "number_of_seats", nullable = false)
    @Min(value = 1, message = "Количество мест должно быть больше или равно 1")
    private Integer numberOfSeats;

    @Column(name = "ticket_price", nullable = false)
    @DecimalMin(value = "0.0", inclusive = true, message = "Цена билета должна быть больше или равна 0")
    private BigDecimal ticketPrice;
    @Column
    private EventStatus status;
    @Column
    private String description;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"event", "client"}) //Поле event уже игнорируется в TicketReservation: ага, да нет уж
    private List<TicketReservation> ticketReservations = new ArrayList<>();

    public Event() {

    }

    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDate.now();
        }
        if (ticketPrice == null) {
            ticketPrice = BigDecimal.ZERO;
        }
        if (status == null) {
            status = EventStatus.PLANNED;
        }
        if (numberOfSeats == null) {
            numberOfSeats = 1;
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

    public Event(String name, LocalDate date, Integer numberOfSeats, BigDecimal ticketPrice, EventStatus status, String description) {
        this.name = name;
        this.date = date;
        this.numberOfSeats =numberOfSeats;
        this.ticketPrice = ticketPrice;
        this.status = status;
        this.description = description;
    }

    public static Event createForTesting(String name, LocalDate date, Integer numberOfSeats, BigDecimal ticketPrice, EventStatus status, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Event event = new Event(name, date, numberOfSeats, ticketPrice, status, description);
        event.setCreatedAt(createdAt);
        event.setUpdatedAt(updatedAt);
        return event;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(Integer numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
                if (oldReservation != null && this.equals(oldReservation.getEvent())) {
                    oldReservation.assignEvent(null);
                }
            }
        }

        this.ticketReservations = newReservations != null ?
                new ArrayList<>(newReservations) : new ArrayList<>();

        for (TicketReservation newReservation : this.ticketReservations) {
            if (newReservation != null) {
                newReservation.assignEvent(this);
            }
        }

    }

    public void addTicketReservation(TicketReservation ticketReservation) {
        ticketReservation.assignEvent(this);
        this.ticketReservations.add(ticketReservation);
    }

    public boolean removeTicketReservation(TicketReservation reservation) {
        boolean removed = this.ticketReservations.remove(reservation);
        if (removed) {
            reservation.assignEvent(null);
        }
        return removed;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date=" + date +
                ", ticketPrice=" + ticketPrice +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}