package com.scrumly.integrationservice.repository;

import com.scrumly.integrationservice.domain.IntegrationServiceEntity;
import com.scrumly.integrationservice.domain.ServiceUsageLogEntity;
import com.scrumly.integrationservice.enums.ServiceUsageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceUsageLogRepository extends JpaRepository<ServiceUsageLogEntity, Long> {
    List<ServiceUsageLogEntity> findAllByUserIdAndServiceNameAndUsageType(String userId, String serviceName, ServiceUsageType usageType);
}
