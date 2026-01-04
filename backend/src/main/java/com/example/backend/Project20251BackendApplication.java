package com.example.backend;

import com.example.backend.configuration.DotenvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Project20251BackendApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Project20251BackendApplication.class);
		app.addInitializers(new DotenvConfig());
		app.run(args);
	}

}
