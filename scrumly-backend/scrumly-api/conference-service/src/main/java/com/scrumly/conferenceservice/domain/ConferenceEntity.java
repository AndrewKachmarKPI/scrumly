package com.scrumly.conferenceservice.domain;

import com.scrumly.conferenceservice.enums.ConferenceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode
@Entity
public class ConferenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false, unique = true)
    private String conferenceId;
    @Column(nullable = false, updatable = false, unique = true)
    private String workspaceId;
    @Column(nullable = false)
    private String creatorId;
    @Column(nullable = false)
    private LocalDateTime createdDate;
    @Column
    @Enumerated(EnumType.STRING)
    private ConferenceStatus status;
}
