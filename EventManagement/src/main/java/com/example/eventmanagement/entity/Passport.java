package com.example.eventmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "passports", uniqueConstraints = {@UniqueConstraint(columnNames = {"series", "number"})})
public class Passport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String series;
    @Column(nullable = false)
    private String number;

//    Соглашения об именовании в разных средах:
//    В Java принято camelCase (например, createdAt)
//    В SQL базах данных часто используют snake_case (например, created_at)
    @Column(name ="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "passport", cascade = CascadeType.ALL) //обратка на себя
    @JsonIgnore
    private Client client;

    public Passport() {

    }

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }

    public Passport(String series, String number) {

        this.series = series;
        this.number = number;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

//    public void setCreatedAt(LocalDateTime createdAt) {
//        this.createdAt = createdAt;
//    } насколько я понимаю не нужен


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passport passport = (Passport) o;
        return Objects.equals(id, passport.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Passport{" +
               "id=" + id +
               ", series='" + series + '\'' +
               ", number='" + number + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}