package com.example.app;

import com.example.app.services.FileStorageService;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class AppApplication /*implements CommandLineRunner*/ {
//	@Resource
//	FileStorageService storageService;
	public static void main(String[] args) {

		//SpringApplication.run(AppApplication.class, args);
		new SpringApplicationBuilder(AppApplication.class)
				.profiles("dev")
				.run(args);
	}
//	@Override
//	public void run(String... args) throws Exception {
//		storageService.deleteAll();
//		storageService.init();
//	}
}
