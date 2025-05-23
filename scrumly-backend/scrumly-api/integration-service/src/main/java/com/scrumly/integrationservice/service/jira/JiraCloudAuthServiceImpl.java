package com.scrumly.integrationservice.service.jira;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.integrationservice.dto.ServiceAuthorizeRQ;
import com.scrumly.integrationservice.dto.ServiceCredentialsDto;
import com.scrumly.integrationservice.dto.jiraCloud.GetAccessTokenRQ;
import com.scrumly.integrationservice.dto.jiraCloud.GetAccessTokenRS;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.StringJoiner;

import static com.scrumly.integrationservice.utils.SecurityUtils.encodeUrlParam;
import static com.scrumly.integrationservice.utils.SecurityUtils.getUsername;

@Service
@RequiredArgsConstructor
public class JiraCloudAuthServiceImpl implements JiraCloudAuthService {
    private final WebClient jiraCloudAuthClient;

    @Value("${integration.jira-cloud.auth-url}")
    private String authUrl;
    @Value("${integration.jira-cloud.audience}")
    private String audience;
    @Value("${integration.jira-cloud.scope}")
    private String scope;
    @Value("${integration.jira-cloud.client-id}")
    private String clientId;
    @Value("${integration.jira-cloud.client-secret}")
    private String clientSecret;
    @Value("${integration.jira-cloud.redirect-uri}")
    private String redirectUri;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Override
    public ServiceCredentialsDto authorize(ServiceAuthorizeRQ authorizeRQ) {
        GetAccessTokenRQ rq = GetAccessTokenRQ.builder()
                .grantType("authorization_code")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .code(authorizeRQ.getCode())
                .redirectUri(redirectUri)
                .build();

        GetAccessTokenRS rs = null;
        try {
            rs = jiraCloudAuthClient.post()
                    .uri("/oauth/token")
                    .bodyValue(rq)
                    .retrieve()
                    .bodyToMono(GetAccessTokenRS.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }

        if (rs == null) {
            throw new ServiceErrorException("Failed to integrate with the Jira Cloud");
        }

        return ServiceCredentialsDto.builder()
                .serviceType(ServiceType.JIRA_CLOUD)
                .connectionId(authorizeRQ.getConnectingId())
                .accessToken(rs.getAccessToken())
                .expiresIn(rs.getExpiresIn())
                .scope(rs.getScope())
                .refreshToken(rs.getRefreshToken())
                .build();
    }

    @Override
    public ServiceCredentialsDto refreshToken(ServiceCredentialsDto serviceCredentialsDto) {
        GetAccessTokenRQ rq = GetAccessTokenRQ.builder()
                .grantType("refresh_token")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .refreshToken(serviceCredentialsDto.getRefreshToken())
                .build();

        GetAccessTokenRS rs = null;
        try {
            rs = jiraCloudAuthClient.post()
                    .uri("/oauth/token")
                    .bodyValue(rq)
                    .retrieve()
                    .bodyToMono(GetAccessTokenRS.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }

        if (rs == null) {
            throw new ServiceErrorException("Failed to integrate with the Jira Cloud");
        }

        return ServiceCredentialsDto.builder()
                .serviceType(ServiceType.JIRA_CLOUD)
                .connectionId(serviceCredentialsDto.getConnectionId())
                .accessToken(rs.getAccessToken())
                .expiresIn(rs.getExpiresIn())
                .scope(rs.getScope())
                .refreshToken(rs.getRefreshToken())
                .build();
    }

    @Override
    public String getAuthorizationUrl() {
        String baseUrl = String.format("%s/authorize", authUrl);
        StringJoiner queryParams = new StringJoiner("&")
                .add("audience=" + encodeUrlParam(audience))
                .add("client_id=" + encodeUrlParam(clientId))
                .add("scope=" + encodeUrlParam(scope))
                .add("redirect_uri=" + encodeUrlParam(redirectUri))
                .add("state=" + encodeUrlParam(getUsername()))
                .add("response_type=code")
                .add("prompt=consent");
        return baseUrl + "?" + queryParams;
    }
}
