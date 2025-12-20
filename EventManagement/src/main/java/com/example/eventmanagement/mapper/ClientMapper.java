package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.ClientCreateWithDependenciesDto;
import com.example.eventmanagement.dto.ClientDoneDto;
import com.example.eventmanagement.dto.ClientDto;
import com.example.eventmanagement.dto.ClientCreateDto;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {

    // ========== TO DTO ==========

    ClientDoneDto toClientDoneDto(Client client);
    List<ClientDoneDto> toClientDoneDtoList(List<Client> clients);

    // ========== FROM DTO (CREATE) ==========
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "passport", ignore = true)
    @Mapping(target = "ticketReservations", ignore = true)
    Client fromCreateWithoutDependenciesDto(ClientCreateWithDependenciesDto dto);

}