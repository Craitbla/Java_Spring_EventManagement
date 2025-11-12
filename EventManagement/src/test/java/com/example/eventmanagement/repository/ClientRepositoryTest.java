package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void whenFindByPhoneNumber_thenReturnClient() {
        // given
        Passport passport = new Passport("1234", "567890");
        entityManager.persist(passport);

        Client client = new Client();
        client.setFullName("Иван Иванов");
        client.setPhoneNumber("+79123456789");
        client.setEmail("ivan@example.com");
        client.setPassport(passport);
        entityManager.persist(client);
        entityManager.flush();

        // when
        Optional<Client> found = clientRepository.findByPhoneNumber("+79123456789");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Иван Иванов");
    }

    @Test
    void whenFindByNonExistentPhoneNumber_thenReturnEmpty() {
        // when
        Optional<Client> found = clientRepository.findByPhoneNumber("+79999999999");

        // then
        assertThat(found).isEmpty();
    }
}