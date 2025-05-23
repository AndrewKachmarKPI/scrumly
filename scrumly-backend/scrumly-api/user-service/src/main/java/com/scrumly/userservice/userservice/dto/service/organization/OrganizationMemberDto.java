package com.scrumly.userservice.userservice.dto.service.organization;

import com.scrumly.userservice.userservice.dto.service.MemberHistoryDto;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import com.scrumly.userservice.userservice.dto.service.invite.InviteDto;
import com.scrumly.userservice.userservice.enums.members.MemberStatus;
import com.scrumly.userservice.userservice.enums.organization.OrganizationMemberRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationMemberDto {
    private Long id;
    private String organizationId;
    private OrganizationMemberRole role;
    private LocalDateTime joinDateTime;
    private MemberStatus status;
    private UserProfileDto profile;
    private InviteDto invite;
    private List<MemberHistoryDto> changeHistory;
}
