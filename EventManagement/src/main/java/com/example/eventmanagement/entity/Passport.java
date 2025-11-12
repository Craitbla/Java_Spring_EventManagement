package com.example.eventmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "passports", uniqueConstraints = {@UniqueConstraint(columnNames = {"series", "number"})})
public class Passport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Pattern(regexp = "\\d{4}", message = "Серия должна содержать 4 цифры")
    @Column(nullable = false)
    private String series;
    @Pattern(regexp = "\\d{6}", message = "Нормер должен содержать 6 цифр")
    @Column(nullable = false)
    private String number;

//    Соглашения об именовании в разных средах:
//    В Java принято camelCase (например, createdAt)
//    В SQL базах данных часто используют snake_case (например, created_at)
    @Column(name ="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnoreProperties("passport")
    @OneToOne(mappedBy = "passport") //обратка на себя
    private Client client;

    public Passport() {

    }

    @PrePersist
    protected void onCreate(){
        if(createdAt==null){
            createdAt = LocalDateTime.now();
        }

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

    public Client getClient() {
        return client;
    }

    protected void assignClient(Client client) {
        this.client = client;
    }

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