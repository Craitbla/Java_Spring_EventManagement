package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.ClientDto;
import com.example.eventmanagement.dto.ClientCreateDto;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING) // Для интеграции со Spring
public interface ClientMapper {
    ClientDto toClientDto(Client client);
    List<ClientDto> toClientDtoList(List<Client> clients);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "passport", ignore = true)
    @Mapping(target = "ticketReservations", ignore = true)
    Client fromClientDto(ClientDto clientDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "passport", ignore = true)
    Client fromClientCreateDto(ClientCreateDto clientCreateDto);


    // ========== UPDATE FROM DTO ==========

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "passport", ignore = true)
    void updateFromDTO(ClientDto clientDto, @MappingTarget Client client);

}
