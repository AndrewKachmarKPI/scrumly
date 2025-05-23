package com.scrumly.integrationservice;

import com.scrumly.integrationservice.service.IntegrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableFeignClients
public class IntegrationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationServiceApplication.class, args);
    }


    @Bean
    CommandLineRunner runner(IntegrationService integrationService) {
        return args -> {
            integrationService.init();
        };
    }
}
