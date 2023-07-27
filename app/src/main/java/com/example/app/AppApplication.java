package com.example.app;

import com.example.app.services.FileStorageService;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppApplication implements CommandLineRunner {
	@Resource
	FileStorageService storageService;
	public static void main(String[] args) {

		SpringApplication.run(AppApplication.class, args);

	}
	@Override
	public void run(String... args) throws Exception {
		storageService.deleteAll();
		storageService.init();
	}
}
