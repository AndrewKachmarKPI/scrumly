package com.scrumly.integrationservice.dto.jiraCloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
public class GetAccessTokenRQ {
    @JsonProperty("grant_type")
    private final String grantType;

    @JsonProperty("client_id")
    private final String clientId;

    @JsonProperty("client_secret")
    private final String clientSecret;

    @JsonProperty("code")
    private final String code;

    @JsonProperty("redirect_uri")
    private final String redirectUri;

    @JsonProperty("refresh_token")
    private final String refreshToken;
}
