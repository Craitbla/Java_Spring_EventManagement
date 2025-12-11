package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.TicketReservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TicketReservationMapper {

    // ========== TO DTO ==========
    TicketReservationDto toTicketReservationDto(TicketReservation ticketReservation);
    List<TicketReservationDto> toTicketReservationDtoList(List<TicketReservation> ticketReservations);
    TicketReservationDoneDto toTicketReservationDoneDto(TicketReservation ticketReservation);
    // ========== FROM DTO (CREATE) ==========
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "event", ignore = true)
    TicketReservation fromCreateWithoutDependenciesDto(TicketReservationCreateDto dto);

    // ========== UPDATE METHODS ==========

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateBasicInfo(TicketReservationCreateDto dto, @MappingTarget TicketReservation ticketReservation);
}
