package com.scrumly.integrationservice.domain;

import com.scrumly.integrationservice.enums.IntegrationServiceScope;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Entity
public class IntegrationServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String serviceName;
    @Column
    private Long activeConnections;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IntegrationServiceScope serviceScope;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ServiceUsageLogEntity> usageLog;
}
