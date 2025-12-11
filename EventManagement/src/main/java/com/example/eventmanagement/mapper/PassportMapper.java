package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PassportMapper {
    PassportDto toPassportDto(Passport passport);
    List<PassportDto> toPassportDtoList(List<Passport> passports);
    @Mapping(target = "updatedAt", ignore = true)
    PassportDoneDto toPassportDoneDto(Passport passport);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "client", ignore = true)
    Passport fromCreateWithoutDependenciesDto(PassportCreateDto dto);



}