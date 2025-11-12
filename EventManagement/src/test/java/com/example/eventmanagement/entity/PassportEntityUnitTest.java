package com.example.eventmanagement.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PassportEntityUnitTest {

    @Test
    void shouldSetTimestampOnCreate() {
        Passport passport = new Passport();
        passport.onCreate();

        assertThat(passport.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldCreatePassportWithSeriesAndNumber() {
        Passport passport = new Passport("1234", "567890");

        assertThat(passport.getSeries()).isEqualTo("1234");
        assertThat(passport.getNumber()).isEqualTo("567890");
    }

    @Test
    void shouldAssignClientBidirectional() {
        Passport passport = new Passport("1234", "567890");
        Client client = new Client();

        passport.assignClient(client);

        assertThat(passport.getClient()).isEqualTo(client);
    }

    @Test
    void shouldHandleNullClientAssignment() {
        Passport passport = new Passport("1234", "567890");
        Client client = new Client();

        passport.assignClient(client);
        passport.assignClient(null);

        assertThat(passport.getClient()).isNull();
    }

    @Test
    void shouldMaintainEqualsAndHashCodeConsistency() {
        Passport passport1 = new Passport("1234", "567890");
        Passport passport2 = new Passport("1234", "567890");
        Passport passport3 = new Passport("9999", "000000");

        passport1.setId(1L);
        passport2.setId(1L);
        passport3.setId(2L);

        assertThat(passport1).isEqualTo(passport2);
        assertThat(passport1).isNotEqualTo(passport3);
        assertThat(passport1.hashCode()).isEqualTo(passport2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithNull() {
        Passport passport = new Passport("1234", "567890");
        passport.setId(1L);

        assertThat(passport).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualWithDifferentClass() {
        Passport passport = new Passport("1234", "567890");
        passport.setId(1L);

        assertThat(passport).isNotEqualTo("not a passport");
    }

    @Test
    void toStringShouldContainImportantFields() {
        Passport passport = new Passport("1234", "567890");
        passport.setId(1L);
        passport.onCreate();

        String toString = passport.toString();

        assertThat(toString).contains("id=1");
        assertThat(toString).contains("series='1234'");
        assertThat(toString).contains("number='567890'");
        assertThat(toString).contains("createdAt=");
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1, true",
            "1, 2, false",
            "2, 1, false"
    })
    void testEqualsWithVariousIds(Long id1, Long id2, boolean expectedEqual) {
        Passport passport1 = new Passport();
        Passport passport2 = new Passport();

        passport1.setId(id1);
        passport2.setId(id2);

        if (expectedEqual) {
            assertThat(passport1).isEqualTo(passport2);
        } else {
            assertThat(passport1).isNotEqualTo(passport2);
        }
    }

    @Test
    void shouldUpdateSeriesAndNumber() {
        Passport passport = new Passport("1234", "567890");

        passport.setSeries("9999");
        passport.setNumber("888888");

        assertThat(passport.getSeries()).isEqualTo("9999");
        assertThat(passport.getNumber()).isEqualTo("888888");
    }
}