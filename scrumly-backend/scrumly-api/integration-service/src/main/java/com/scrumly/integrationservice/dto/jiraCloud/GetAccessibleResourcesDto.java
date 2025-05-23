package com.scrumly.integrationservice.dto.jiraCloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
public class GetAccessibleResourcesDto {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("url")
    private String url;

    @JsonProperty("scopes")
    private List<String> scopes;

    @JsonProperty("avatarUrl")
    private String avatarUrl;
}
