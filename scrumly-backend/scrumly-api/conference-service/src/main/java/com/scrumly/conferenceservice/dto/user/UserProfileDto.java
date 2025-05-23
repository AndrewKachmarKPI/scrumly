package com.scrumly.conferenceservice.dto.user;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private Long id;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String bio;
    private String phoneNumber;
    private LocalDateTime registered;
    private String avatarId;
    private LocalDateTime lastActivity;
    private LocalDate dateOfBirth;
    private List<String> skills;
}
