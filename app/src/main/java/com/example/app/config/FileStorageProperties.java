package com.example.app.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.nio.file.Path;

@Component
@ConfigurationProperties("file-storage")
@Data
@Slf4j
public class FileStorageProperties{
    private Path root;
    private Path timesheet;
    private Path evaluation;
    public void logFileStorageProperties(){
        log.info("FileStorageProperties Initialized with the following properties:");
        log.info("Root Directory: " + root.toString());
        log.info("Timesheets Directory: " + timesheet.toString());
        log.info("Evaluations Directory: " + evaluation.toString());
    }
}
