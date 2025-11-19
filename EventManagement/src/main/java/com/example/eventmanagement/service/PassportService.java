package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.PassportCreateDto;
import com.example.eventmanagement.dto.PassportDto;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.exception.DuplicateEntityException;
import com.example.eventmanagement.exception.EntityNotFoundException;
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
    //плевать что с Passport
    public Passport createPassport(PassportCreateDto passportCreateDto) {
        Passport passportForSaving = passportMapper.fromCreateWithoutDependenciesDto(passportCreateDto);
        if (passportRepository.existsBySeriesAndNumber(passportForSaving.getSeries(), passportForSaving.getNumber())) {
            throw new DuplicateEntityException("Такой пасспорт уже есть");
        }
        Passport savedPassport = passportRepository.save(passportForSaving);
        return savedPassport;

    }

    public void deletePassport(Long id) {
        Passport passport = passportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пасспорт с id %d не найден", id)
                ));
        passportRepository.delete(passport);

    }

//    public Optional<PassportDto> savePassport(){
// нет потому что это обновление паспорта, его не будет, только черз клиента напрямую

}
