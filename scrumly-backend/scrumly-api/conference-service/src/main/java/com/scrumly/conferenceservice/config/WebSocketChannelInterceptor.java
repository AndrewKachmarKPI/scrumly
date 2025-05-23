package com.scrumly.conferenceservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;


public class WebSocketChannelInterceptor implements ChannelInterceptor {
    @Autowired
    private JwtDecoder jwtDecoder;

    enum StompCommands {
        CONNECT, SUBSCRIBE, DISCONNECT
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        MessageHeaders headers = message.getHeaders();
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        MultiValueMap<String, String> multiValueMap = headers.get(StompHeaderAccessor.NATIVE_HEADERS, MultiValueMap.class);
        StompCommand stompCommand = headers.get("stompCommand", StompCommand.class);
        if (stompCommand == null) {
         return message;
        }
        if (StompCommands.CONNECT.toString().equals(stompCommand.getMessageType().toString()) && multiValueMap != null && multiValueMap.containsKey(HttpHeaders.AUTHORIZATION)) {
            String authorizationHeader = multiValueMap.getFirst(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(jwt, null, authorities);
                    accessor.setUser(authentication);
                } catch (JwtException e) {
                    System.out.println("Invalid JWT token: " + e.getMessage());
                    throw new AccessDeniedException("Access Denied");
                }
            }
        } else {
            if (StompCommands.CONNECT.toString().equals(stompCommand.getMessageType().toString())) {
                throw new AccessDeniedException("Access denied");
            }
        }
        return message;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            return roles.stream()
                    .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
