package com.scrumly.integrationservice.service;

import com.scrumly.enums.integration.ServiceType;
import com.scrumly.integrationservice.dto.ServiceCredentialsDto;

public interface ServiceCredentialsService {
    void saveCredentials(ServiceCredentialsDto credentialsDto);

    void updateCredentials(ServiceCredentialsDto credentialsDto);
    void updateCredentialsWithoutOwner(ServiceCredentialsDto credentialsDto);

    void removeCredentials(String connectionId, ServiceType serviceType);

    Boolean isConnected(String connectionId, ServiceType serviceType);
    Boolean isConnectionOwner(String connectionId, ServiceType serviceType);

    ServiceCredentialsDto findCredentials(String connectionId, ServiceType serviceType);
    ServiceCredentialsDto findCredentialsWithoutOwner(String connectionId, ServiceType serviceType);

}
