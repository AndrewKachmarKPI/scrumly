package com.scrumly.integrationservice.repository;

import com.scrumly.integrationservice.domain.IntegrationServiceEntity;
import com.scrumly.integrationservice.enums.IntegrationServiceScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegrationServiceRepository extends JpaRepository<IntegrationServiceEntity, Long> {
    IntegrationServiceEntity findByServiceName(String serviceName);
    List<IntegrationServiceEntity> findAllByServiceScope(IntegrationServiceScope scope);
}
