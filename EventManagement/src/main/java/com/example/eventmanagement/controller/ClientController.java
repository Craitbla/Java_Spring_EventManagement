package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.exception.EntityNotFoundException;
import com.example.eventmanagement.service.ClientService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/clients")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<ClientDto>> getAllClients() {
        log.info("GET /api/clients - получение списка всех клиентов");
        List<ClientDto> clients = clientService.getAll();
        log.debug("Найдено клиентов: {}", clients.size());
        return  ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDoneDto> getClientById(@PathVariable Long id) {
        log.info("GET /api/client - получение клиента по id {}", id);
       ClientDoneDto client = clientService.getById(id);
        log.info("Клиент с id {} найден");
        return  ResponseEntity.ok(client);
    }

    @GetMapping("/searchTerm")
    public ResponseEntity<List<ClientDto>>  searchClient(@PathVariable String searchTerm) { //написано что можно и другое
        log.info("GET /api/searchTerm - получение клиента по строчке для поиска");
        List<ClientDto> clients  = clientService.searchClients(searchTerm);
        log.debug("Найдено клиентов: {}", clients.size());
        return  ResponseEntity.ok(clients);
    }


    @PostMapping
    public ResponseEntity<ClientDoneDto> createClient(@Valid @RequestBody ClientCreateWithDependenciesDto dto){
        log.info("POST /api - создание клиента: {}", dto.email());
        ClientDoneDto createdClient = clientService.createClient(dto);
        log.info("Клиент создан с id {}", createdClient.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDoneDto> updateClient(@PathVariable Long id, @Valid @RequestBody ClientCreateDto dto){
        log.info("PUT /api/client - обновление клиента с id: {}", id);
        ClientDoneDto updatedClient = clientService.updateClientBasicInfo(id, dto);
        log.info("Клиент с id {} обновлен", id);
        return ResponseEntity.ok(updatedClient);
    }

    @PutMapping("/{id}/passport")
    public ResponseEntity<ClientDoneDto> updatePassport(@PathVariable Long id, @Valid @RequestBody PassportCreateDto dto){
        log.info("PUT /api/client/passport - обновление паспорта клиента с id: {}", id);
        ClientDoneDto updatedClient = clientService.replacePassport(id, dto);
        log.info("Паспорт клиента с id {} обновлен", id);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id){
        log.info("DELETE /api/client - удаление клиента с id: {}", id);
        deleteClient(id);
        log.info("Клиент с id {} удален", id);
        return  ResponseEntity.noContent().build();
    }




}
