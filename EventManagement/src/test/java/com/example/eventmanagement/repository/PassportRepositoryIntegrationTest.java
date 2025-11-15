package com.example.eventmanagement.repository;

import com.example.eventmanagement.entity.Client;
import com.example.eventmanagement.entity.Passport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PassportRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PassportRepository passportRepository;

    private Passport passport1;

    @Nested
    class PassportRepositoryBasicTests {

        @BeforeEach
        void setUp() {
            passport1 = new Passport("1234", "567890");
            entityManager.persist(passport1);
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        void shouldFindBySeriesAndNumber() {
            Optional<Passport> foundPassport = passportRepository.findBySeriesAndNumber("1234", "567890");
            assertThat(foundPassport).isPresent();
            assertThat(foundPassport.get().getSeries()).isEqualTo("1234");
            assertThat(foundPassport.get().getNumber()).isEqualTo("567890");
        }

        @Test
        void shouldNotFindByNonExistentSeriesAndNumber() {
            Optional<Passport> foundPassport = passportRepository.findBySeriesAndNumber("9999", "999999");
            assertThat(foundPassport).isEmpty();
        }

        @Test
        void shouldCheckExistsBySeriesAndNumber() {
            assertThat(passportRepository.existsBySeriesAndNumber("1234", "567890")).isTrue();
            assertThat(passportRepository.existsBySeriesAndNumber("9999", "999999")).isFalse();
        }

        @Test
        void shouldSaveAndRetrievePassport() {
            Passport newPassport = new Passport("5555", "666666");
            Passport saved = passportRepository.save(newPassport);

            assertThat(saved.getId()).isNotNull();

            Optional<Passport> found = passportRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getSeries()).isEqualTo("5555");
            assertThat(found.get().getNumber()).isEqualTo("666666");
        }
    }

    @Nested
    class PassportRepositoryDateTimeTests {
        private Passport passport2, passport3;

        @BeforeEach
        void setUp() {
            LocalDateTime now = LocalDateTime.now();

            passport1 = Passport.createForTesting("1111", "222222", now.minusDays(2));
            passport2 = Passport.createForTesting("3333", "444444", now.minusDays(1));
            passport3 = Passport.createForTesting("5555", "666666", now);

            entityManager.persist(passport1);
            entityManager.persist(passport2);
            entityManager.persist(passport3);
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        void shouldFindByCreatedAtBefore() {
            LocalDateTime threshold = LocalDateTime.now().minusHours(12);
            List<Passport> foundPassports = passportRepository.findByCreatedAtBefore(threshold);
            assertThat(foundPassports).hasSize(2);
        }

        @Test
        void shouldFindByCreatedAtBetween() {
            LocalDateTime start = LocalDateTime.now().minusDays(3);
            LocalDateTime end = LocalDateTime.now().minusDays(1).plusHours(1);
            List<Passport> foundPassports = passportRepository.findByCreatedAtBetween(start, end);
            assertThat(foundPassports).hasSize(2);
        }

        @Test
        void shouldFindByCreatedAtAfter() {
            LocalDateTime threshold = LocalDateTime.now().minusHours(12);
            List<Passport> foundPassports = passportRepository.findByCreatedAtAfter(threshold);
            assertThat(foundPassports).hasSize(1);
        }

        @Test
        void shouldFindByCreatedAtExact() {
            LocalDateTime exactTime = passport2.getCreatedAt();
            List<Passport> foundPassports = passportRepository.findByCreatedAtBetween(
                    exactTime.minusSeconds(1), exactTime.plusSeconds(1));
            assertThat(foundPassports).hasSize(1);
            assertThat(foundPassports.get(0)).isEqualTo(passport2);
        }
    }

    @Nested
    class PassportRepositoryAdvancedTests {

        @Test
        void shouldFindByIdWithClient() {
            passport1 = new Passport("1234", "567890");
            entityManager.persist(passport1);
            Client client = new Client("Test Client", "+79123456789", "test@mail.com", passport1);
            entityManager.persist(client);
            entityManager.flush();
            entityManager.clear();

            Optional<Passport> foundPassport = passportRepository.findByIdWithClient(passport1.getId());

            assertThat(foundPassport).isPresent();
            assertThat(foundPassport.get().getClient()).isNotNull();
            assertThat(foundPassport.get().getClient().getFullName()).isEqualTo("Test Client");
        }

        @Test
        void shouldReturnEmptyForNonExistentIdWithClient() {
            passport1 = new Passport("1234", "567890");
            entityManager.persist(passport1);
            entityManager.flush();
            entityManager.clear();
            Optional<Passport> foundPassport = passportRepository.findByIdWithClient(999L);
            assertThat(foundPassport).isEmpty();
        }

        @Test
        void shouldEnforceUniqueSeriesAndNumberConstraint() {
            passport1 = new Passport("1234", "567890");
            entityManager.persist(passport1);
            entityManager.flush();
            entityManager.clear();
            Passport duplicatePassport = new Passport("1234", "567890");

            assertThat(org.junit.jupiter.api.Assertions.assertThrows(
                    Exception.class,
                    () -> {
                        passportRepository.saveAndFlush(duplicatePassport);
                    }
            )).isInstanceOf(Exception.class);
        }
    }
}