package com.scrumly.eventservice.domain.blocks.reflect;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
public class ReflectColumnEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Integer columnOrder;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String color;
    @Column
    private String instruction;

    public ReflectColumnEntity(ReflectColumnEntity columnEntity) {
        this.columnOrder = columnEntity.columnOrder;
        this.title = columnEntity.title;
        this.color = columnEntity.color;
        this.instruction = columnEntity.instruction;
    }
}
