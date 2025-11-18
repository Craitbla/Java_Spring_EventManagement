package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.PassportCreateDto;
import com.example.eventmanagement.dto.PassportDto;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.exception.DuplicateEntityException;
import com.example.eventmanagement.mapper.PassportMapper;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.PassportRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class PassportService {

    //после досоздания разобраться сразу с логированием
    private final PassportRepository passportRepository;
    private final PassportMapper passportMapper;

    PassportService(PassportRepository passportRepository, PassportMapper passportMapper) {
        this.passportRepository = passportRepository;
        this.passportMapper = passportMapper;

    }

    //буду кэтчить видимо дальше
    public PassportDto createPassport(PassportCreateDto passportCreateDto) {
        Passport passportForSaving = passportMapper.fromPassportCreateDto(passportCreateDto);
        if (passportRepository.existsBySeriesAndNumber(passportForSaving.getSeries(), passportForSaving.getNumber())) {
            throw new DuplicateEntityException("Такой пасспорт уже есть");
        }
        Passport savedPassport = passportRepository.save(passportForSaving);
        return passportMapper.toPassportDto(savedPassport);

    }

//    public Optional<PassportDto> savePassport(){
// нет потому что это обновление паспорта, его не будет, только черз клиента напрямую

}
