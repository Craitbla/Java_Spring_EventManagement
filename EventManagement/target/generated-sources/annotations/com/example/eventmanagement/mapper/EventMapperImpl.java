package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.EventCreateDto;
import com.example.eventmanagement.dto.EventDoneDto;
import com.example.eventmanagement.dto.EventDto;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.enums.EventStatus;
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
public class EventMapperImpl implements EventMapper {

    @Override
    public EventDto toEventDto(Event event) {
        if ( event == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        LocalDate date = null;
        Integer numberOfSeats = null;
        BigDecimal ticketPrice = null;
        EventStatus status = null;
        String description = null;

        id = event.getId();
        name = event.getName();
        date = event.getDate();
        numberOfSeats = event.getNumberOfSeats();
        ticketPrice = event.getTicketPrice();
        status = event.getStatus();
        description = event.getDescription();

        EventDto eventDto = new EventDto( id, name, date, numberOfSeats, ticketPrice, status, description );

        return eventDto;
    }

    @Override
    public List<EventDto> toEventDtoList(List<Event> events) {
        if ( events == null ) {
            return null;
        }

        List<EventDto> list = new ArrayList<EventDto>( events.size() );
        for ( Event event : events ) {
            list.add( toEventDto( event ) );
        }

        return list;
    }

    @Override
    public EventDoneDto toEventDoneDto(Event event) {
        if ( event == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        LocalDate date = null;
        Integer numberOfSeats = null;
        BigDecimal ticketPrice = null;
        EventStatus status = null;
        String description = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = event.getId();
        name = event.getName();
        date = event.getDate();
        numberOfSeats = event.getNumberOfSeats();
        ticketPrice = event.getTicketPrice();
        status = event.getStatus();
        description = event.getDescription();
        createdAt = event.getCreatedAt();
        updatedAt = event.getUpdatedAt();

        EventDoneDto eventDoneDto = new EventDoneDto( id, name, date, numberOfSeats, ticketPrice, status, description, createdAt, updatedAt );

        return eventDoneDto;
    }

    @Override
    public Event fromCreateWithoutDependenciesDto(EventCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Event event = new Event();

        event.setName( dto.name() );
        event.setDate( dto.date() );
        event.setNumberOfSeats( dto.numberOfSeats() );
        event.setTicketPrice( dto.ticketPrice() );
        event.setDescription( dto.description() );

        return event;
    }
}
