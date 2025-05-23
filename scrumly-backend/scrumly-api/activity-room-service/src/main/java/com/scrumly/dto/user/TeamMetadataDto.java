package com.scrumly.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class TeamMetadataDto {
    private String organizationId;
    private String teamId;
    private String organizationName;
    private String teamName;
}
