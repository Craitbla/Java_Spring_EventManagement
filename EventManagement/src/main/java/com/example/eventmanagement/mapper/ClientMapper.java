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
//    ClientDto toClientDto(Client client);
//    List<ClientDto> toClientDtoList(List<Client> clients);

    ClientDoneDto toClientDoneDto(Client client);
    List<ClientDoneDto> toClientDoneDtoList(List<Client> clients);

    // ========== FROM DTO (CREATE) ==========
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "passport", ignore = true)        // Создаем отдельно
    @Mapping(target = "ticketReservations", ignore = true)
    Client fromCreateWithoutDependenciesDto(ClientCreateWithDependenciesDto dto);
//создает без
    // ========== UPDATE METHODS ==========

    // Обновление только основных полей (имя, телефон, email)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "passport", ignore = true)        // Не трогаем паспорт
    @Mapping(target = "ticketReservations", ignore = true)
    void updateBasicInfo(ClientCreateDto dto, @MappingTarget Client client);
    //здесь оставила ClientCreateDt потому что  ClientCreateDt и ClientUpdateDto были бы одинаковыми

    // Обновление только паспорта (отдельный метод)
    // Здесь маппер не нужен - работаем напрямую с сущностями
}