package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.PassportCreateDto;
import com.example.eventmanagement.dto.PassportDoneDto;
import com.example.eventmanagement.dto.PassportDto;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.exception.DuplicateEntityException;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.exception.OperationNotAllowedException;
import com.example.eventmanagement.mapper.PassportMapper;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.PassportRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class PassportService {
    //По итогу вообще не нужен оказался, круто, класс, ладно

    //после досоздания разобраться сразу с логированием
    private final PassportRepository passportRepository;
    private final PassportMapper passportMapper;

    PassportService(PassportRepository passportRepository, PassportMapper passportMapper) {
        this.passportRepository = passportRepository;
        this.passportMapper = passportMapper;

    }
//по идее не нужен потому что паспорт сохраняется каскадно

//    public PassportDoneDto updatePassport(Long id, PassportCreateDto dto) {
//        //найти, обновить, засейвить
//        Passport passportForUpdating = passportRepository.findById(id).orElseThrow(
//                () -> new EntityNotFoundException("Паспорт с id " +id+ " для обновления не найден")
//        );
//        passportForUpdating.setSeries(dto.series());
//        passportForUpdating.setNumber(dto.number());
//
//        Passport updatedPassport = passportRepository.save(passportForUpdating);
//        return passportMapper.toPassportDoneDto(updatedPassport);
//
//    }

}
