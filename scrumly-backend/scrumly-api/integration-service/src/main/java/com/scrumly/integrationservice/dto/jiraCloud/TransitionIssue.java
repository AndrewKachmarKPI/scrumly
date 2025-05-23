package com.scrumly.integrationservice.dto.jiraCloud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransitionIssue {
    private Transition transition;

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transition {
        private String id;
    }
}
