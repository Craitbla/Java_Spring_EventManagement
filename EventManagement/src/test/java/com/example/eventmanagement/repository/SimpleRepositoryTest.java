package com.example.eventmanagement.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class SimpleRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void repositoryIsInjected() {
        assertTrue(clientRepository != null);
    }

    @Test
    void findAllReturnsEmptyListWhenNoData() {
        assertTrue(clientRepository.findAll().isEmpty());
    }
}