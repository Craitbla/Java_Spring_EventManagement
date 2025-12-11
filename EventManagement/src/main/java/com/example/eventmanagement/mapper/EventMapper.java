package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    // ========== TO DTO ==========
    EventDto toEventDto(Event event);
    List<EventDto> toEventDtoList(List<Event> events);
    EventDoneDto toEventDoneDto(Event event);

    // ========== FROM DTO (CREATE) ==========
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ticketReservations", ignore = true)
    @Mapping(target = "status", ignore = true)
    Event fromCreateWithoutDependenciesDto(EventCreateDto dto);

}
