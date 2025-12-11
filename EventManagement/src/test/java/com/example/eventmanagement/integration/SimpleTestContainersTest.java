package com.example.eventmanagement.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

//Проверяет какая БД используется
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("testcontainers")
class SimpleTestContainersTest extends BaseTestcontainersTest{

    @Autowired
    private JdbcTemplate jdbcTemplate;



    @Test
    void shouldUsePostgreSQL() throws SQLException {
        String url = jdbcTemplate.getDataSource().getConnection().getMetaData().getURL();
        String dbProduct = jdbcTemplate.getDataSource().getConnection().getMetaData().getDatabaseProductName();

        System.out.println("Database URL: " + url);
        System.out.println("Database Product: " + dbProduct);

        assertThat(url).contains("jdbc:postgresql:");
        assertThat(dbProduct).isEqualTo("PostgreSQL");
    }

    @Test
    void shouldExecuteUniversalQuery() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertThat(result).isEqualTo(1);
    }
    @Test
    void shouldConnectToDatabase() {
        try {
            String dbProduct = jdbcTemplate.getDataSource()
                    .getConnection()
                    .getMetaData()
                    .getDatabaseProductName();

            System.out.println("Database: " + dbProduct);

            String result = jdbcTemplate.queryForObject("SELECT 1", String.class);
            assertThat(result).isEqualTo("1");

            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM information_schema.tables", Integer.class);
            assertThat(count).isGreaterThanOrEqualTo(0);

            System.out.println("Successfully connected to: " + dbProduct);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void shouldConnectToPostgreSQL() {
        try {
            String dbProduct = jdbcTemplate.getDataSource()
                    .getConnection()
                    .getMetaData()
                    .getDatabaseProductName();

            System.out.println("Database: " + dbProduct);

            if ("PostgreSQL".equals(dbProduct)) {
                String result = jdbcTemplate.queryForObject("SELECT version()", String.class);
                assertThat(result).contains("PostgreSQL");
                System.out.println("PostgreSQL version: " + result);
            } else if ("H2".equals(dbProduct)) {
                String result = jdbcTemplate.queryForObject("SELECT H2VERSION()", String.class);
                assertThat(result).isNotNull();
                System.out.println("H2 version: " + result);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}