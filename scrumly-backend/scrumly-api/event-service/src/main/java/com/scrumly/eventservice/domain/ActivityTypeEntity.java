package com.scrumly.eventservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Entity
public class ActivityTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String type;
    @Column(nullable = false)
    private String color;
    @Column(nullable = false)
    private LocalDateTime dateTimeCreated;

    public ActivityTypeEntity(ActivityTypeEntity typeEntity) {
        this.id = typeEntity.id;
        this.type = typeEntity.type;
        this.dateTimeCreated = typeEntity.dateTimeCreated;
    }
}
