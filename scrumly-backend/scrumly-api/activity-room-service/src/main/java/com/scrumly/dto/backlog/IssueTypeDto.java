package com.scrumly.dto.backlog;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class IssueTypeDto {
    private Long id;
    @NotNull
    private String backlogId;
    @NotNull
    private String type;
    private String iconUrl;
    @NotNull
    private Boolean isDefault;
}
