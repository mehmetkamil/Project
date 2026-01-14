package com.yeditepe.firstspingproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "com.yeditepe.firstspingproject.entity")
@EnableJpaRepositories(basePackages = "com.yeditepe.firstspingproject.repository")
@EnableAsync
@EnableScheduling
public class FirstspingprojectApplication {

	public static void main(String[] args) {

		SpringApplication.run(FirstspingprojectApplication.class, args);
	}

}
