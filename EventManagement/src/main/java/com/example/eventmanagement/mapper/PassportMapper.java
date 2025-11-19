package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING) // Для интеграции со Spring
public interface PassportMapper {
    PassportDto toPassportDto(Passport passport);
    List<PassportDto> toPassportDtoList(List<Passport> passports);
    PassportDoneDto toPassportDoneDto(Passport passport);

    //по идее toCreate никогда не нужен будет вроде как, только если в логах
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "client", ignore = true)
    Passport fromCreateWithoutDependenciesDto(PassportCreateWithDependenciesDto dto);

//UPDATE FROM DTO это конечно очень весело, но именно в пасспорте не нужно обновление



}