package com.scrumly.integrationservice.repository;

import com.scrumly.enums.integration.ServiceType;
import com.scrumly.integrationservice.domain.ServiceCredentialsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCredentialsRepository extends JpaRepository<ServiceCredentialsEntity, Long> {
    ServiceCredentialsEntity findByConnectionIdAndServiceType(String connectionId, ServiceType serviceType);
    ServiceCredentialsEntity findByConnectionIdAndConnectionOwnerAndServiceType(String connectionId, String connectionOwner, ServiceType serviceType);
}
