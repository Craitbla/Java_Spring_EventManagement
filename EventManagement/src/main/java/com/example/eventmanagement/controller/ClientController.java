package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<ClientDto>> getAllClients() {
        List<ClientDto> clients = clientService.getAll();
        return  ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDoneDto> getClientById(@PathVariable Long id) {
       ClientDoneDto client = clientService.getById(id);
        return  ResponseEntity.ok(client);
    }

    @GetMapping("/searchTerm")
    public ResponseEntity<List<ClientDto>>  searchClient(@PathVariable String searchTerm) { //написано что можно и другое
        List<ClientDto> clients  = clientService.searchClients(searchTerm);
        return  ResponseEntity.ok(clients);
    }


    @PostMapping
    public ResponseEntity<ClientDoneDto> createClient(@Valid @RequestBody ClientCreateWithDependenciesDto dto){
        ClientDoneDto createdClient = clientService.createClient(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
    }
//    @PostMapping
//    public ResponseEntity<ClientDoneDto> createClient(@Valid @RequestBody ClientCreateWithDependenciesDto dto){
//        try{
//            ClientDoneDto createdClient = clientService.createClient(dto);
//            return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
//        } catch (EntityNotFoundException e){
//            return ResponseEntity.badRequest();
//        }
//    }

    @PutMapping("/{id}")//и потом отдельно для паспорта
    public ResponseEntity<ClientDoneDto> updateClient(@PathVariable Long id, @Valid @RequestBody ClientCreateDto dto){
        ClientDoneDto updatedClient = clientService.updateClientBasicInfo(id, dto);
        return ResponseEntity.ok(updatedClient);
    }

    @PutMapping("/{id}/passport")
    public ResponseEntity<ClientDoneDto> updatePassport(@PathVariable Long id, @Valid @RequestBody PassportCreateDto dto){
        ClientDoneDto updatedClient = clientService.replacePassport(id, dto);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id){
        deleteClient(id);
        return  ResponseEntity.noContent().build();
    }




}
