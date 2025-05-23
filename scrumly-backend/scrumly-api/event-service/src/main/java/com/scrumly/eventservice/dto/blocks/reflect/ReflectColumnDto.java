package com.scrumly.eventservice.dto.blocks.reflect;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class ReflectColumnDto {
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
}
