package com.example.app.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@TestConfiguration
@Profile("integration")
public class IntegrationDatasourceConfig {

    @Bean
    public DataSource dataSource(EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.unwrap(DataSource.class);
    }
}
