package com.example.trainsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.example.trainsservice.repository")
@EntityScan(basePackages = "com.example.trainsservice.model")
public class TrainsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainsServiceApplication.class, args);
    }

}