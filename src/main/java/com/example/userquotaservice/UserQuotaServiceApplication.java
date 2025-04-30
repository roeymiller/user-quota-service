package com.example.userquotaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UserQuotaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserQuotaServiceApplication.class, args);
	}

}
