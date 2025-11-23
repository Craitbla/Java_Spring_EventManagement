package com.example.eventmanagement.controller.unit;

import com.example.eventmanagement.controller.AdminController;
import com.example.eventmanagement.dto.CleanupResponse;
import com.example.eventmanagement.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerUnitTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void cleanupOldCanceledReservations_WhenReservationsDeleted_ShouldReturnOkWithCount() {
        when(adminService.cleanupOldCanceledReservations()).thenReturn(5);

        ResponseEntity<CleanupResponse> response = adminController.cleanupOldCanceledReservations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().deletedCount());
        assertEquals("Удалено 5 бронирований", response.getBody().message());
        verify(adminService).cleanupOldCanceledReservations();
    }

    @Test
    void cleanupOldCanceledReservations_WhenNoReservations_ShouldReturnOkWithNoDeletionsMessage() {
        when(adminService.cleanupOldCanceledReservations()).thenReturn(0);

        ResponseEntity<CleanupResponse> response = adminController.cleanupOldCanceledReservations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().deletedCount());
        assertEquals("Нет старых отмененных бронирований для очистки", response.getBody().message());
        verify(adminService).cleanupOldCanceledReservations();
    }
}