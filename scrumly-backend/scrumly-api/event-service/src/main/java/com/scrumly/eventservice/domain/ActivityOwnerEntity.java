package com.scrumly.eventservice.domain;

import com.scrumly.eventservice.enums.ActivityScope;
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
public class ActivityOwnerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String createdById;
    @Column
    private String ownerId;
    @Column(nullable = false)
    private LocalDateTime dateTimeCreated;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityScope scope;

    public ActivityOwnerEntity(ActivityOwnerEntity owner) {
        this.id = owner.id;
        this.createdById = owner.createdById;
        this.ownerId = owner.ownerId;
        this.dateTimeCreated = owner.dateTimeCreated;
        this.scope = owner.scope;
    }
}
