package com.scrumly.userservice.userservice.dto.service.user;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class UserServiceConnectionDto {
    private Long id;
    private String serviceName;
    private LocalDateTime dateConnected;
}
