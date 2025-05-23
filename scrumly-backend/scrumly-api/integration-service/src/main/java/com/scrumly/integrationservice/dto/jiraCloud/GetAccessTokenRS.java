package com.scrumly.integrationservice.dto.jiraCloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
public class GetAccessTokenRS {
    @JsonProperty("access_token")
    private final String accessToken;
    @JsonProperty("token_type")
    private final String tokenType;
    @JsonProperty("expires_in")
    private final Long expiresIn;
    @JsonProperty("refresh_token")
    private final String refreshToken;
    @JsonProperty("scope")
    private final String scope;
    @JsonProperty("created_at")
    private final Long createdAt;
}
