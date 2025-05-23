package com.scrumly.userservice.userservice.dto.service.user;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private List<UserServiceConnectionDto> connectedServices;
    private List<OrganizationConnectionDto> connectedOrganizations;
}
