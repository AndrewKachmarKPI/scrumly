package com.scrumly.integrationservice.service.jira;

import com.scrumly.integrationservice.dto.ServiceAuthorizeRQ;
import com.scrumly.integrationservice.dto.ServiceCredentialsDto;

public interface JiraCloudAuthService {
    String getAuthorizationUrl();

    ServiceCredentialsDto authorize(ServiceAuthorizeRQ authorizeRQ);

    ServiceCredentialsDto refreshToken(ServiceCredentialsDto serviceCredentialsDto);
}
