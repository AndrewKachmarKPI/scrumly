package com.scrumly.dto.backlog;

import com.scrumly.enums.integration.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class BacklogIssueStatusesDto {
    private String backlogId;
    private String backlogName;
    private ServiceType serviceType;
    private List<IssueStatusDto> statusList;
}
