package com.scrumly.config;

import com.scrumly.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class WebSocketChannelInterceptor implements ChannelInterceptor {
    private JwtDecoder jwtDecoder;

    public WebSocketChannelInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    enum StompCommands {
        CONNECT, SUBSCRIBE, DISCONNECT, MESSAGE
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);


        if (nonNull(accessor) && (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                StompCommand.SEND.equals(accessor.getCommand()))) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (isNull(authHeader) || !authHeader.startsWith("Bearer" + " ")) {
                throw new AccessDeniedException("You are not authorized!");
            }

            String token = authHeader.substring("Bearer".length() + 1);
            try {
                Jwt jwt = jwtDecoder.decode(token);
                Collection<GrantedAuthority> authorities = SecurityUtils.extractAuthorities(jwt);
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
                accessor.setUser(authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException e) {
                e.printStackTrace();
                System.out.println("Invalid JWT token: " + e.getMessage());
                throw new AccessDeniedException("You are not authorized!");
            }
        }

        return message;
    }
}
