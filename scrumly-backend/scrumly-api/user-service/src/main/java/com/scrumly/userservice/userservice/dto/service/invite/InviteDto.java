package com.scrumly.userservice.userservice.dto.service.invite;

import com.scrumly.userservice.userservice.dto.service.organization.OrganizationInfoDto;
import com.scrumly.userservice.userservice.dto.service.team.TeamDto;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import com.scrumly.userservice.userservice.enums.invite.InviteStatus;
import com.scrumly.userservice.userservice.enums.invite.InviteType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class InviteDto {
    private Long id;
    private String inviteId;
    private String inviteUrl;
    private String connectingId;
    private InviteType inviteType;
    private InviteStatus currentStatus;
    private UserProfileDto createBy;
    private UserProfileDto createdFor;
    private LocalDateTime created;
    private LocalDateTime accepted;
    private LocalDateTime expiresAt;
    private Boolean isExpired;
    private List<InviteHistoryDto> changeLog;
    private OrganizationInfoDto orgInfoDto;
    private TeamDto teamInfo;
    private Boolean hasManagePermission;
}
