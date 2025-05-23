package com.scrumly.integrationservice.dto.jiraCloud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetTransitions {
    private String expand;
    private List<Transition> transitions;

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transition {
        private String id;
        private String name;
        private To to;
        private boolean hasScreen;
        private boolean isGlobal;
        private boolean isInitial;
        private boolean isAvailable;
        private boolean isConditional;
        private boolean isLooped;
    }

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class To {
        private String self;
        private String description;
        private String iconUrl;
        private String name;
        private String id;
        private StatusCategory statusCategory;
    }

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCategory {
        private String self;
        private int id;
        private String key;
        private String colorName;
        private String name;
    }
}
