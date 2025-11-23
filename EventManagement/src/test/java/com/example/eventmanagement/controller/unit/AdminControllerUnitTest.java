package com.example.eventmanagement.controller.unit;

import com.example.eventmanagement.controller.AdminController;
import com.example.eventmanagement.dto.CleanupResponse;
import com.example.eventmanagement.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void cleanupOldCanceledReservations_shouldReturnSuccessMessageWhenReservationsDeleted() throws Exception {
        int deletedCount = 5;
        when(adminService.cleanupOldCanceledReservations()).thenReturn(deletedCount);

        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        mockMvc.perform(post("/api/admin/cleanup/canceled-reservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(deletedCount))
                .andExpect(jsonPath("$.message").value("Удалено " + deletedCount + " бронирований"));
    }

    @Test
    void cleanupOldCanceledReservations_shouldReturnNoReservationsMessageWhenNoneDeleted() throws Exception {
        int deletedCount = 0;
        when(adminService.cleanupOldCanceledReservations()).thenReturn(deletedCount);

        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        mockMvc.perform(post("/api/admin/cleanup/canceled-reservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(deletedCount))
                .andExpect(jsonPath("$.message").value("Нет старых отмененных бронирований для очистки"));
    }
}