package com.scrumly.conferenceservice.config;

import io.openvidu.java.client.OpenVidu;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenViduConfig {
    @Value("${open-vidu.url}")
    public String OPENVIDU_URL;

    @Value("${open-vidu.secret}")
    private String OPENVIDU_SECRET;

    @Bean
    public OpenVidu openvidu() {
        return new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }
}
