package com.scrumly.eventservice.dto;

import com.scrumly.validations.SqlInjectionSafe;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class ActivityTypeDto {
    private Long id;
    @NotNull
    @NotEmpty
    @NotBlank
    @Size(max = 200)
    @SqlInjectionSafe
    private String type;
    private String color;
    private LocalDateTime dateTimeCreated;
}
