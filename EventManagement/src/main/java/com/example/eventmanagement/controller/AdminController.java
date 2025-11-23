package com.example.eventmanagement.controller;
import com.example.eventmanagement.dto.CleanupResponse;
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
    public ResponseEntity<CleanupResponse> cleanupOldCanceledReservations() {
        int deletedCount = adminService.cleanupOldCanceledReservations();

        if (deletedCount > 0) {
            return ResponseEntity.ok(new CleanupResponse(deletedCount, "Удалено " + deletedCount + " бронирований"));
        } else {
            return ResponseEntity.ok(new CleanupResponse(deletedCount,"Нет старых отмененных бронирований для очистки"));
        }
    }

}