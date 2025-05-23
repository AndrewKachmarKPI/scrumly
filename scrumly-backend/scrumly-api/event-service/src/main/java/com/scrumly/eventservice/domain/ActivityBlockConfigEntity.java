package com.scrumly.eventservice.domain;

import com.scrumly.eventservice.enums.ActivityBlockType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Entity
public class ActivityBlockConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Integer blockOrder;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityBlockType blockType;
    @Column(nullable = false)
    private boolean isOptional;
    @Column(nullable = false)
    private String blockId;

    public ActivityBlockConfigEntity(ActivityBlockConfigEntity config) {
        this.blockOrder = config.blockOrder;
        this.blockType = config.blockType;
        this.isOptional = config.isOptional;
        this.blockId = config.blockId;
    }
}
