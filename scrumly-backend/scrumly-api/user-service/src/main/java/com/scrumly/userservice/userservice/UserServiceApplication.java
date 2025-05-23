package com.scrumly.userservice.userservice;

import com.scrumly.userservice.userservice.dto.keycloak.KeycloakRoleDto;
import com.scrumly.userservice.userservice.enums.UserRole;
import com.scrumly.userservice.userservice.services.KeycloakInitService;
import com.scrumly.userservice.userservice.services.KeycloakService;
import com.scrumly.userservice.userservice.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
@EnableFeignClients
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }


    @Bean
    public CommandLineRunner runner(KeycloakInitService initService) {
        return args -> {
            initService.init();
        };
    }
}
