package com.scrumly.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
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
