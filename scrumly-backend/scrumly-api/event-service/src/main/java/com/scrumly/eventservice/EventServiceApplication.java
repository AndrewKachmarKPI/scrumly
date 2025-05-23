package com.scrumly.eventservice;

import com.scrumly.eventservice.services.ActivityInitService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableFeignClients
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }


    @Bean
    CommandLineRunner commandLineRunner(ActivityInitService activityInitService) {
        return args -> {
            activityInitService.initDefaultActivity();
        };
    }
}
