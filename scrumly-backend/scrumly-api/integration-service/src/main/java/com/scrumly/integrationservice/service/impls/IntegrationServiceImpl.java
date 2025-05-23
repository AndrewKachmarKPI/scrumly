package com.scrumly.integrationservice.service.impls;

import com.scrumly.enums.integration.ServiceType;
import com.scrumly.exceptions.enums.ServiceErrorCode;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.integrationservice.domain.IntegrationServiceEntity;
import com.scrumly.integrationservice.domain.ServiceUsageLogEntity;
import com.scrumly.integrationservice.dto.IntegrationServiceDto;
import com.scrumly.integrationservice.dto.ServiceAuthorizeRQ;
import com.scrumly.integrationservice.dto.ServiceCredentialsDto;
import com.scrumly.integrationservice.dto.ServiceRefreshRQ;
import com.scrumly.integrationservice.dto.jiraCloud.GetAccessibleResourcesDto;
import com.scrumly.integrationservice.enums.IntegrationServiceScope;
import com.scrumly.integrationservice.enums.ServiceUsageType;
import com.scrumly.integrationservice.repository.IntegrationServiceRepository;
import com.scrumly.integrationservice.service.google.GoogleCalendarService;
import com.scrumly.integrationservice.service.IntegrationService;
import com.scrumly.integrationservice.service.jira.JiraCloudApiService;
import com.scrumly.integrationservice.service.jira.JiraCloudAuthService;
import com.scrumly.integrationservice.service.ServiceCredentialsService;
import com.scrumly.integrationservice.service.jira.JiraConnectionResourceService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.scrumly.integrationservice.utils.SecurityUtils.getUsername;

@Service
@RequiredArgsConstructor
public class IntegrationServiceImpl implements IntegrationService {
    private final ModelMapper modelMapper;
    private final IntegrationServiceRepository serviceRepository;
    private final ServiceCredentialsService credentialsService;
    private final GoogleCalendarService googleCalendarService;
    private final JiraCloudAuthService jiraCloudAuthService;
    private final JiraCloudApiService jiraCloudApiService;
    private final JiraConnectionResourceService jiraConnectionResourceService;

    @Override
    @Transactional
    public void authorize(ServiceAuthorizeRQ authorizeRQ) {
        try {
            ServiceCredentialsDto credentialsDto = null;
            if (authorizeRQ.getServiceType().equals(ServiceType.GOOGLE_CALENDAR)) {
                credentialsDto = googleCalendarService.authorize(authorizeRQ);
            } else if (authorizeRQ.getServiceType().equals(ServiceType.JIRA_CLOUD)) {
                credentialsDto = jiraCloudAuthService.authorize(authorizeRQ);
            }
            if (credentialsDto != null) {
                credentialsService.saveCredentials(credentialsDto);
                updateServiceUsage(authorizeRQ.getServiceType(), ServiceUsageType.CONNECT);
            }

            onSuccessfulAuthorize(authorizeRQ);
        } catch (Exception e) {
            credentialsService.removeCredentials(authorizeRQ.getConnectingId(), authorizeRQ.getServiceType());
            throw new ServiceErrorException(e);
        }
    }

    private void onSuccessfulAuthorize(ServiceAuthorizeRQ authorizeRQ) {
        if (authorizeRQ.getServiceType().equals(ServiceType.JIRA_CLOUD)) {
            List<GetAccessibleResourcesDto> resources = jiraCloudApiService.getAccessibleResources(authorizeRQ.getConnectingId());
            jiraConnectionResourceService.saveConnectionRecourses(authorizeRQ.getConnectingId(), resources);
        }
    }

    @Override
    public void disconnect(String connectionId, ServiceType serviceType) {
        credentialsService.removeCredentials(connectionId, serviceType);
        updateServiceUsage(serviceType, ServiceUsageType.DISCONNECT);
    }

    @Override
    @Transactional
    public void refresh(ServiceRefreshRQ refreshRQ) {
        if (refreshRQ.getServiceType().equals(ServiceType.JIRA_CLOUD)) {
            ServiceCredentialsDto credentialsDto = credentialsService
                    .findCredentials(refreshRQ.getConnectingId(), ServiceType.JIRA_CLOUD);
            ServiceCredentialsDto updatedCredentials = jiraCloudAuthService.refreshToken(credentialsDto);
            credentialsService.updateCredentials(updatedCredentials);
        }
        onSuccessfulRefresh(refreshRQ);
    }

    private void onSuccessfulRefresh(ServiceRefreshRQ refreshRQ) {
        if (refreshRQ.getServiceType().equals(ServiceType.JIRA_CLOUD)) {
            List<GetAccessibleResourcesDto> resources = jiraCloudApiService.getAccessibleResources(refreshRQ.getConnectingId());
            jiraConnectionResourceService.saveConnectionRecourses(refreshRQ.getConnectingId(), resources);
        }
    }

    @Override
    public List<IntegrationServiceDto> findIntegrationServices(String connectionId, IntegrationServiceScope scope) {
        return serviceRepository.findAllByServiceScope(scope).stream()
                .map(service -> modelMapper.map(service, IntegrationServiceDto.class).toBuilder()
                        .connectionId(connectionId)
                        .isConnected(credentialsService.isConnected(connectionId, ServiceType.valueOf(service.getServiceName())))
                        .isConnectionOwner(credentialsService.isConnectionOwner(connectionId, ServiceType.valueOf(service.getServiceName())))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceType> findConnectedServices(String connectionId) {
        List<IntegrationServiceDto> serviceDtos = findIntegrationServices(connectionId, IntegrationServiceScope.ORGANIZATION);
        return serviceDtos.stream()
                .filter(IntegrationServiceDto::getIsConnected)
                .map(integrationServiceDto -> ServiceType.valueOf(integrationServiceDto.getServiceName()))
                .toList();
    }

    @Override
    public Boolean isConnected(ServiceType serviceType) {
        return credentialsService.isConnected(getUsername(), serviceType);
    }

    @Override
    public Boolean isConnected(ServiceType serviceType, String connectionId) {
        return credentialsService.isConnected(connectionId, serviceType);
    }

    public void updateServiceUsage(ServiceType serviceType, ServiceUsageType type) {
        try {
            IntegrationServiceEntity service = serviceRepository.findByServiceName(serviceType.toString());
            if (service == null) {
                throw new ServiceErrorException(ServiceErrorCode.SERVICE_NOTFOUND);
            }
            ServiceUsageLogEntity log = ServiceUsageLogEntity.builder()
                    .userId(getUsername())
                    .dateAction(LocalDateTime.now())
                    .usageType(type)
                    .serviceName(serviceType.toString())
                    .build();
            service.getUsageLog().add(log);
            long totalConnected = type.equals(ServiceUsageType.CONNECT)
                    ? service.getActiveConnections() + 1
                    : service.getActiveConnections() - 1;
            service.setActiveConnections(totalConnected);
            serviceRepository.save(service);
        } catch (ServiceErrorException e) {
            throw new ServiceErrorException(e);
        }
    }


    @Override
    public void init() {
        if (serviceRepository.findByServiceName(ServiceType.GOOGLE_CALENDAR.toString()) == null) {
            serviceRepository.save(IntegrationServiceEntity.builder()
                                           .serviceName(ServiceType.GOOGLE_CALENDAR.toString())
                                           .activeConnections(0L)
                                           .usageLog(new ArrayList<>())
                                           .serviceScope(IntegrationServiceScope.MEMBER)
                                           .build());
        }
        if (serviceRepository.findByServiceName(ServiceType.JIRA_CLOUD.toString()) == null) {
            serviceRepository.save(IntegrationServiceEntity.builder()
                                           .serviceName(ServiceType.JIRA_CLOUD.toString())
                                           .activeConnections(0L)
                                           .usageLog(new ArrayList<>())
                                           .serviceScope(IntegrationServiceScope.ORGANIZATION)
                                           .build());
        }
    }
}
