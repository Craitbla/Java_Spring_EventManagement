package com.example.eventmanagement.controller;

import com.example.eventmanagement.dto.*;
import com.example.eventmanagement.service.ClientService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDoneDto> getClientById(@PathVariable Long id) {
        log.info("GET /api/clients/{} - получение клиента по ID", id);
        ClientDoneDto client = clientService.getById(id);
        log.info("Клиент с id {} найден: {}", id, client.fullName());
        return ResponseEntity.ok(client);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClientDto>> searchClient(@RequestParam String searchTerm) {
        log.info("GET /api/clients/search?searchTerm={} - поиск клиентов", searchTerm);
        List<ClientDto> clients = clientService.searchClients(searchTerm);
        log.debug("По запросу '{}' найдено клиентов: {}", searchTerm, clients.size());
        return ResponseEntity.ok(clients);
    }

    @PostMapping
    public ResponseEntity<ClientDoneDto> createClient(@Valid @RequestBody ClientCreateWithDependenciesDto dto) {
        log.info("POST /api/clients - создание клиента: {} ({})", dto.fullName(), dto.email());
        ClientDoneDto createdClient = clientService.createClient(dto);
        log.info("Клиент создан с id {}: {}", createdClient.id(), createdClient.fullName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDoneDto> updateClient(@PathVariable Long id, @Valid @RequestBody ClientCreateDto dto) {
        log.info("PUT /api/clients/{} - обновление клиента", id);
        ClientDoneDto updatedClient = clientService.updateClientBasicInfo(id, dto);
        log.info("Клиент с id {} обновлен: {} -> {}", id, updatedClient.fullName(), updatedClient.email());
        return ResponseEntity.ok(updatedClient);
    }

    @PutMapping("/{id}/passport")
    public ResponseEntity<ClientDoneDto> updatePassport(@PathVariable Long id, @Valid @RequestBody PassportCreateDto dto) {
        log.info("PUT /api/clients/{}/passport - обновление паспорта", id);
        ClientDoneDto updatedClient = clientService.replacePassport(id, dto);
        log.info("Паспорт клиента с id {} обновлен: {} {}", id, dto.series(), dto.number());
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        log.info("DELETE /api/clients/{} - удаление клиента", id);
        clientService.deleteClient(id);
        log.info("Клиент с id {} удален", id);
        return ResponseEntity.noContent().build();
    }
}