package com.scrumly.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(auth -> auth
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/users/**").permitAll()
                        .pathMatchers("/integrations/**").permitAll()
                        .pathMatchers("/events/**").permitAll()
                        .pathMatchers("/conference/**").permitAll()
                        .pathMatchers("/room/**").permitAll()
                        .pathMatchers("/configuration/ui",
                                      "/swagger-resources/**",
                                      "/configuration/**",
                                      "/swagger-ui.html",
                                      "/webjars/**",
                                      "/v3/api-docs/**",
                                      "/user-service/v3/api-docs/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(withDefaults()));
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }
}
