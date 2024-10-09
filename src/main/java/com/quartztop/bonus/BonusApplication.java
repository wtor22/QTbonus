package com.quartztop.bonus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BonusApplication {

	public static void main(String[] args) {
		SpringApplication.run(BonusApplication.class, args);
	}

}
