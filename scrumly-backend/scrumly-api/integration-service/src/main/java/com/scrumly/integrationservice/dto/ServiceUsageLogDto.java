package com.scrumly.integrationservice.dto;

import com.scrumly.integrationservice.enums.ServiceUsageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ServiceUsageLogDto {
    private String userId;
    private LocalDateTime dateAction;
    private ServiceUsageType usageType;
}
