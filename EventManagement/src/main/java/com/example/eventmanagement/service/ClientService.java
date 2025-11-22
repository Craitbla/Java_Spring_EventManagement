package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import com.example.eventmanagement.enums.BookingStatus;
import com.example.eventmanagement.exception.BusinessValidationException;
import com.example.eventmanagement.exception.DuplicateEntityException;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.exception.OperationNotAllowedException;
import com.example.eventmanagement.mapper.ClientMapper;
import com.example.eventmanagement.mapper.PassportMapper;
import com.example.eventmanagement.repository.ClientRepository;
import com.example.eventmanagement.repository.PassportRepository;
import com.example.eventmanagement.repository.TicketReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ClientService {
    private final ClientRepository clientRepository;
    private final PassportRepository passportRepository;
    private final TicketReservationRepository ticketReservationRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, PassportRepository passportRepository, TicketReservationRepository ticketReservationRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.passportRepository = passportRepository;
        this.ticketReservationRepository = ticketReservationRepository;
        this.clientMapper = clientMapper;
    }

    public List<ClientDto> getAll() {
        return clientMapper.toClientDtoList(clientRepository.findAll());
    }
    public ClientDoneDto getById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Клиент с id %d не найден", id)
                ));
        return clientMapper.toClientDoneDto(client);
    }

    public ClientDoneDto createClient(ClientCreateWithDependenciesDto dto) {

        if (clientRepository.existsByEmail(dto.email())) {
            throw new DuplicateEntityException("Клиент c таким email " + dto.email() + " уже существует");
        }
        if (clientRepository.existsByPhoneNumber(dto.phoneNumber())) {
            throw new DuplicateEntityException("Клиент c таким телефоном " + dto.phoneNumber() + " уже существует");
        }
        PassportCreateDto passportDto = dto.passport();
        if (passportRepository.existsBySeriesAndNumber(passportDto.series(), passportDto.number())) {
            throw new DuplicateEntityException("Клиент c таким паспортом " + passportDto.number() + passportDto.series() + " уже существует");
        }
        //каскад
        Client client = new Client(
                dto.fullName(),
                dto.phoneNumber(),
                dto.email(),
                new Passport(passportDto.series(), passportDto.number())
        );
        Client savedClient = clientRepository.save(client);
        return clientMapper.toClientDoneDto(savedClient);
    }

    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Клиент с id %d не найден", id)
                ));
        if(!canDeleteClient(id)){
            throw new BusinessValidationException(String.format("Этого клиента с id %d нельзя удалить, у него еще есть активные бронирования", id));
        }
        clientRepository.delete(client);
    }
    public List<ClientDto> searchClients(String searchTerm) {
        List<Client> foundedClients = clientRepository.searchClients(searchTerm);
        return clientMapper.toClientDtoList(foundedClients);
    }
    public boolean canDeleteClient(Long clientId) {
        List<BookingStatus> bookingStatusList = new ArrayList<>();
        bookingStatusList.add(BookingStatus.CONFIRMED);
        bookingStatusList.add(BookingStatus.PENDING_CONFIRMATION);
        return ticketReservationRepository.findByClientIdAndBookingStatusIn(clientId, bookingStatusList).isEmpty();
    }
    public ClientDoneDto updateClientBasicInfo(Long id, ClientCreateDto dto) { //пока один
        Client client = clientRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Клиент с id %d не найден", id))
        );
        if (clientRepository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new DuplicateEntityException("Клиент c таким email " + dto.email() + " уже существует");
        }
        if (clientRepository.existsByPhoneNumberAndIdNot(dto.phoneNumber(), id)) {
            throw new DuplicateEntityException("Клиент c таким телефоном " + dto.phoneNumber() + " уже существует");
        }
        client.setFullName(dto.fullName());
        client.setPhoneNumber(dto.phoneNumber());
        client.setEmail(dto.email());

        //проверить существует ли    ок
        //проверить такой же ли, если другой то
        //удалить старый, похуй, сделаю через сервис паспорта
        Client updatedClient = clientRepository.save(client);
        return clientMapper.toClientDoneDto(updatedClient);
        //насколько я понимаю flush потом в контроллере
    }

    public ClientDoneDto replacePassport(Long clientId, PassportCreateDto newPassportDto){
        Client client = clientRepository.findById(clientId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Клиент с id %d не найден", clientId))
        );
        Passport oldPassport = client.getPassport();
        if (passportRepository.existsBySeriesAndNumber(
                newPassportDto.series(), newPassportDto.number())) {
            throw new DuplicateEntityException("Паспорт " +newPassportDto.series()+" "+ newPassportDto.number()+ " уже существует");
        }
        Passport newPassport = new Passport(newPassportDto.series(), newPassportDto.number());

        // старый НЕ удалится каскадно, надо вручную
        client.setPassport(newPassport);
        Client updatedClient = clientRepository.save(client);

        if(oldPassport!=null){
            passportRepository.delete(oldPassport);
        }

        return clientMapper.toClientDoneDto(updatedClient);
    }

}

