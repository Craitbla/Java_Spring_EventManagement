package com.example.eventmanagement.controller.unit;

import com.example.eventmanagement.controller.AdminController;
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
    void cleanupOldCanceledReservations_WhenReservationsDeleted_ShouldReturnOkWithCount() throws Exception {
        when(adminService.cleanupOldCanceledReservations()).thenReturn(5);

        ResponseEntity<String> response = adminController.cleanupOldCanceledReservations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Удалено 5 старых отмененных бронирований", response.getBody());
        verify(adminService).cleanupOldCanceledReservations();
    }

    @Test
    void cleanupOldCanceledReservations_WhenNoReservations_ShouldReturnOkWithNoDeletionsMessage() throws Exception {
        when(adminService.cleanupOldCanceledReservations()).thenReturn(0);

        ResponseEntity<String> response = adminController.cleanupOldCanceledReservations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Нет старых отмененных бронирований для очистки", response.getBody());
        verify(adminService).cleanupOldCanceledReservations();
    }
}