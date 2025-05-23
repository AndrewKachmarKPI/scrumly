package com.scrumly.eventservice.domain;

import com.scrumly.eventservice.enums.ActivityBlockType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@MappedSuperclass
public class ActivityBlockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, updatable = false)
    private String blockId;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityBlockType type;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column
    private Boolean isMandatory = Boolean.TRUE;


    public ActivityBlockEntity(ActivityBlockEntity block) {
        this.blockId = block.blockId;
        this.type = block.type;
        this.name = block.name;
        this.description = block.description;
        this.isMandatory = block.isMandatory;
    }
}
