package com.example.eventmanagement.util.testutils;

import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class TestDataHelper {

    public static TicketReservation createOldCanceledReservation(
            Client client, Event event, LocalDateTime oldDateTime) {

        TicketReservation reservation = new TicketReservation(2, BookingStatus.CANCELED);

        client.addTicketReservation(reservation);
        event.addTicketReservation(reservation);

        try {
            Field createdAtField = TicketReservation.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(reservation, oldDateTime);

            Field updatedAtField = TicketReservation.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(reservation, oldDateTime);

        } catch (Exception e) {
            throw new RuntimeException("Failed to set fields via reflection", e);
        }

        return reservation;
    }

    public static TicketReservation createReservationWithCustomDates(
            Client client, Event event, Integer numberOfTickets,
            BookingStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {

        TicketReservation reservation = new TicketReservation(numberOfTickets, status);
        client.addTicketReservation(reservation);
        event.addTicketReservation(reservation);

        try {
            Field createdAtField = TicketReservation.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(reservation, createdAt);

            Field updatedAtField = TicketReservation.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(reservation, updatedAt);

        } catch (Exception e) {
            throw new RuntimeException("Failed to set fields via reflection", e);
        }

        return reservation;
    }
}