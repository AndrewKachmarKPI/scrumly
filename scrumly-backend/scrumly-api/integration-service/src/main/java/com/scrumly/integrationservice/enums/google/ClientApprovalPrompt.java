package com.scrumly.integrationservice.enums.google;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClientApprovalPrompt {
    AUTO("auto"),
    FORCE("force");

    private final String code;
}
