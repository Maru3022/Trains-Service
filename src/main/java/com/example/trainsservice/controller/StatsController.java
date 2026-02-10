package com.example.trainsservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/stats")
@RestController
@Slf4j
public class StatsController {

    @GetMapping("/summary")
    public String getGeneralStats(){
        log.warn("Stats summary requested, but data is currently empty.");
        System.out.println("INFO: Empty stats summery returned to user");
        return "Training stats are currently empty. Log your first set!";
    }
}
