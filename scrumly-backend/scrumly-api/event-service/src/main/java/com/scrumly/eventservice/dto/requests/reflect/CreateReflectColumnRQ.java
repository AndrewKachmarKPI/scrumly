package com.scrumly.eventservice.dto.requests.reflect;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateReflectColumnRQ {
    private Integer columnOrder;
    private String title;
    private String color;
    private String instruction;
}
