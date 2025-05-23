package com.scrumly.integrationservice.dto.jiraCloud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Builder(toBuilder = true)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GetProjectPaginated {
    private List<GetProject> values;

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetProject {
        private String id;
        private String name;
    }
}
