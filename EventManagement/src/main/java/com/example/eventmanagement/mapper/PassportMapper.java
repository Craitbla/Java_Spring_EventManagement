package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.PassportCreateDto;
import com.example.eventmanagement.dto.PassportDto;
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
//    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "client", ignore = true)
    Passport fromPassportDto(PassportDto passportDto);

    //по идее toCreate никогда не нужен будет вроде как, только если в логах
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "client", ignore = true)
    Passport fromPassportCreateDto(PassportCreateDto passportCreateDto);


    // ========== UPDATE FROM DTO ==========

//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "client", ignore = true)
//    void updateFromDTO(PassportDto passportDto, @MappingTarget Passport passport);

//это конечно очень весело, но именно в пасспорте не нужно обновление



}