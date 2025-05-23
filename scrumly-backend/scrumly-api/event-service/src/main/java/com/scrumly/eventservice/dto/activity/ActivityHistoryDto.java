package com.scrumly.eventservice.dto.activity;

import com.scrumly.eventservice.enums.ActivityStatus;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class ActivityHistoryDto {
    private Long id;
    private LocalDateTime dateTime;
    private String performedBy;
    private ActivityStatus previousStatus;
    private ActivityStatus newStatus;
    private String changeDetails;
}
