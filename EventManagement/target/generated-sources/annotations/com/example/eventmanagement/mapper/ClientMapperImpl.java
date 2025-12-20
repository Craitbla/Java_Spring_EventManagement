package com.example.eventmanagement.mapper;

import com.example.eventmanagement.dto.ClientCreateWithDependenciesDto;
import com.example.eventmanagement.dto.ClientDoneDto;
import com.example.eventmanagement.dto.PassportCreateDto;
import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-20T14:48:08+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class ClientMapperImpl implements ClientMapper {

    @Override
    public ClientDoneDto toClientDoneDto(Client client) {
        if ( client == null ) {
            return null;
        }

        Long id = null;
        String fullName = null;
        String phoneNumber = null;
        String email = null;
        PassportCreateDto passport = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = client.getId();
        fullName = client.getFullName();
        phoneNumber = client.getPhoneNumber();
        email = client.getEmail();
        passport = passportToPassportCreateDto( client.getPassport() );
        createdAt = client.getCreatedAt();
        updatedAt = client.getUpdatedAt();

        ClientDoneDto clientDoneDto = new ClientDoneDto( id, fullName, phoneNumber, email, passport, createdAt, updatedAt );

        return clientDoneDto;
    }

    @Override
    public List<ClientDoneDto> toClientDoneDtoList(List<Client> clients) {
        if ( clients == null ) {
            return null;
        }

        List<ClientDoneDto> list = new ArrayList<ClientDoneDto>( clients.size() );
        for ( Client client : clients ) {
            list.add( toClientDoneDto( client ) );
        }

        return list;
    }

    @Override
    public Client fromCreateWithoutDependenciesDto(ClientCreateWithDependenciesDto dto) {
        if ( dto == null ) {
            return null;
        }

        Client client = new Client();

        client.setFullName( dto.fullName() );
        client.setPhoneNumber( dto.phoneNumber() );
        client.setEmail( dto.email() );

        return client;
    }

    protected PassportCreateDto passportToPassportCreateDto(Passport passport) {
        if ( passport == null ) {
            return null;
        }

        String series = null;
        String number = null;

        series = passport.getSeries();
        number = passport.getNumber();

        PassportCreateDto passportCreateDto = new PassportCreateDto( series, number );

        return passportCreateDto;
    }
}
