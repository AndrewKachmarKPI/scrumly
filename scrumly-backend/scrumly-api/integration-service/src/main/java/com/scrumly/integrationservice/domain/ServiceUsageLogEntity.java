package com.scrumly.integrationservice.domain;

import com.scrumly.integrationservice.enums.ServiceUsageType;
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
public class ServiceUsageLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String serviceName;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private LocalDateTime dateAction;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceUsageType usageType;
}
