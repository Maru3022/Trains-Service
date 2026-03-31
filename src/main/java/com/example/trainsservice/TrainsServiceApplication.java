package com.example.trainsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrainsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainsServiceApplication.class, args);
    }

}