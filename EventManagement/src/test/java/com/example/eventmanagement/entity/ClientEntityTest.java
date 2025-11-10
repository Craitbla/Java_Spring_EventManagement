package com.example.eventmanagement.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ClientEntityTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldValidateCorrectClient() {
        // Given
        Client client = new Client("John Doe", "+79123456789", "valid@email.com");

        // When
        Set<ConstraintViolation<Client>> violations = validator.validate(client);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenPhoneInvalid() {
        // Given
        Client client = new Client("John Doe", "invalid-phone", "valid@email.com");

        // When
        Set<ConstraintViolation<Client>> violations = validator.validate(client);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Телефон должен начинаться с +7");
    }

    @Test
    void shouldFailValidationWhenEmailInvalid() {
        // Given
        Client client = new Client("John Doe", "+79123456789", "invalid-email");

        // When
        Set<ConstraintViolation<Client>> violations = validator.validate(client);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Email должен быть валидным");
    }
}