package com.scrumly.userservice.userservice.domain.user;

import com.scrumly.userservice.userservice.domain.ImageEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Entity
public class UserProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, updatable = false)
    private String userId;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column
    private String phoneNumber;
    @Column(length = 500)
    private String bio;
    @Column(nullable = false)
    private LocalDateTime registered;
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private ImageEntity avatar;
    @Column
    private LocalDateTime lastActivity;
    @Column
    private LocalDate dateOfBirth;
    @ElementCollection
    private List<String> skills;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrganizationConnectionEntity> connectedOrganizations = new ArrayList<>();
}
