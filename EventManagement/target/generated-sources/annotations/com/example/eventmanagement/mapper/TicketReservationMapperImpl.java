package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.ClientCreateDto;
import com.example.eventmanagement.dto.EventCreateDto;
import com.example.eventmanagement.dto.TicketReservationCreateDto;
import com.example.eventmanagement.dto.TicketReservationDoneDto;
import com.example.eventmanagement.dto.TicketReservationDto;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.TicketReservation;
import com.example.eventmanagement.enums.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-20T14:48:08+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class TicketReservationMapperImpl implements TicketReservationMapper {

    @Override
    public TicketReservationDto toTicketReservationDto(TicketReservation ticketReservation) {
        if ( ticketReservation == null ) {
            return null;
        }

        Long id = null;
        ClientCreateDto client = null;
        EventCreateDto event = null;
        Integer numberOfTickets = null;
        BookingStatus bookingStatus = null;

        id = ticketReservation.getId();
        client = clientToClientCreateDto( ticketReservation.getClient() );
        event = eventToEventCreateDto( ticketReservation.getEvent() );
        numberOfTickets = ticketReservation.getNumberOfTickets();
        bookingStatus = ticketReservation.getBookingStatus();

        TicketReservationDto ticketReservationDto = new TicketReservationDto( id, client, event, numberOfTickets, bookingStatus );

        return ticketReservationDto;
    }

    @Override
    public List<TicketReservationDto> toTicketReservationDtoList(List<TicketReservation> ticketReservations) {
        if ( ticketReservations == null ) {
            return null;
        }

        List<TicketReservationDto> list = new ArrayList<TicketReservationDto>( ticketReservations.size() );
        for ( TicketReservation ticketReservation : ticketReservations ) {
            list.add( toTicketReservationDto( ticketReservation ) );
        }

        return list;
    }

    @Override
    public TicketReservationDoneDto toTicketReservationDoneDto(TicketReservation ticketReservation) {
        if ( ticketReservation == null ) {
            return null;
        }

        Long id = null;
        ClientCreateDto client = null;
        EventCreateDto event = null;
        Integer numberOfTickets = null;
        BookingStatus bookingStatus = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = ticketReservation.getId();
        client = clientToClientCreateDto( ticketReservation.getClient() );
        event = eventToEventCreateDto( ticketReservation.getEvent() );
        numberOfTickets = ticketReservation.getNumberOfTickets();
        bookingStatus = ticketReservation.getBookingStatus();
        createdAt = ticketReservation.getCreatedAt();
        updatedAt = ticketReservation.getUpdatedAt();

        TicketReservationDoneDto ticketReservationDoneDto = new TicketReservationDoneDto( id, client, event, numberOfTickets, bookingStatus, createdAt, updatedAt );

        return ticketReservationDoneDto;
    }

    @Override
    public TicketReservation fromCreateWithoutDependenciesDto(TicketReservationCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        TicketReservation ticketReservation = new TicketReservation();

        ticketReservation.setNumberOfTickets( dto.numberOfTickets() );
        ticketReservation.setBookingStatus( dto.bookingStatus() );

        return ticketReservation;
    }

    protected ClientCreateDto clientToClientCreateDto(Client client) {
        if ( client == null ) {
            return null;
        }

        String fullName = null;
        String phoneNumber = null;
        String email = null;

        fullName = client.getFullName();
        phoneNumber = client.getPhoneNumber();
        email = client.getEmail();

        ClientCreateDto clientCreateDto = new ClientCreateDto( fullName, phoneNumber, email );

        return clientCreateDto;
    }

    protected EventCreateDto eventToEventCreateDto(Event event) {
        if ( event == null ) {
            return null;
        }

        String name = null;
        LocalDate date = null;
        Integer numberOfSeats = null;
        BigDecimal ticketPrice = null;
        String description = null;

        name = event.getName();
        date = event.getDate();
        numberOfSeats = event.getNumberOfSeats();
        ticketPrice = event.getTicketPrice();
        description = event.getDescription();

        EventCreateDto eventCreateDto = new EventCreateDto( name, date, numberOfSeats, ticketPrice, description );

        return eventCreateDto;
    }
}
