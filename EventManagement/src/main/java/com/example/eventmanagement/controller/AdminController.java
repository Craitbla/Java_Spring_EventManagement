package com.example.eventmanagement.controller;
import com.example.eventmanagement.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/cleanup/canceled-reservations")
    public ResponseEntity<String> cleanupOldCanceledReservations() {
        int deletedCount = adminService.cleanupOldCanceledReservations();

        if (deletedCount > 0) {
            return ResponseEntity.ok("Удалено " + deletedCount + " старых отмененных бронирований");
        } else {
            return ResponseEntity.ok("Нет старых отмененных бронирований для очистки");
        }
    }
}