package com.example.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.nio.file.Files;

@Configuration
@Profile({"dev","unit"})
@RequiredArgsConstructor
@Slf4j
public class FileStorageDirectoriesInitializationConfig {
    private final FileStorageProperties storageProperties;
    @Bean
    public CommandLineRunner initializeDirectories() {
        return args -> {
            init();
        };
    }
    public void init() {
        try {
            if (!Files.exists(storageProperties.getTimesheet())) {
                Files.createDirectories(storageProperties.getTimesheet());
            }
            if (!Files.exists(storageProperties.getEvaluation())) {
                Files.createDirectories(storageProperties.getEvaluation());
            }
            storageProperties.logFileStorageProperties();
        } catch (IOException ignored) {
            log.error("An Error occurred during initialization of file storage directories");
        }
    }
}
