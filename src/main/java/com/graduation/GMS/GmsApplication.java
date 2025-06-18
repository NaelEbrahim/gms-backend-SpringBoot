package com.graduation.GMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class GmsApplication {
	public static void main(String[] args) {
		SpringApplication.run(GmsApplication.class, args);
	}


}
