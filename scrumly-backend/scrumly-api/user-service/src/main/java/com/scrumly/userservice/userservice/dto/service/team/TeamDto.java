package com.scrumly.userservice.userservice.dto.service.team;

import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class TeamDto {
    private Long id;
    private String organizationId;
    private String teamId;
    private String name;
    private LocalDateTime created;
    private UserProfileDto createdBy;
    private Integer totalMembers;
    private List<TeamMemberDto> members;
}
