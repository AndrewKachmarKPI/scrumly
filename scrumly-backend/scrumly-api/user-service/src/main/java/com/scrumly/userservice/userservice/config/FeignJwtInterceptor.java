package com.scrumly.userservice.userservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Component
public class FeignJwtInterceptor implements RequestInterceptor {
    private static final String TOKEN_TYPE = "Bearer";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isNull(authentication) && authentication instanceof JwtAuthenticationToken jwt) {
            requestTemplate.header(HttpHeaders.AUTHORIZATION, String.format("%s %s", TOKEN_TYPE, jwt.getToken().getTokenValue()));
        }
    }
}
