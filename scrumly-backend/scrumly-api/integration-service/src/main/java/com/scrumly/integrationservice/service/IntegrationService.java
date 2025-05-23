package com.scrumly.integrationservice.service;

import com.scrumly.enums.integration.ServiceType;
import com.scrumly.integrationservice.dto.IntegrationServiceDto;
import com.scrumly.integrationservice.dto.ServiceAuthorizeRQ;
import com.scrumly.integrationservice.dto.ServiceRefreshRQ;
import com.scrumly.integrationservice.enums.IntegrationServiceScope;

import java.util.List;


public interface IntegrationService {
    void authorize(ServiceAuthorizeRQ authorizeRQ);

    void disconnect(String connectionId, ServiceType serviceType);

    void refresh(ServiceRefreshRQ refreshRQ);

    List<IntegrationServiceDto> findIntegrationServices(String connectionId, IntegrationServiceScope scope);

    List<ServiceType> findConnectedServices(String connectionId);

    Boolean isConnected(ServiceType serviceType);

    Boolean isConnected(ServiceType serviceType, String connectionId);

    void init();
}
