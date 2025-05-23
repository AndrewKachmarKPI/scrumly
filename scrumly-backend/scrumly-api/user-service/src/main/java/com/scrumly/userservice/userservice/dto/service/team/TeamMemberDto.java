package com.scrumly.userservice.userservice.dto.service.team;


import com.scrumly.userservice.userservice.dto.service.MemberHistoryDto;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import com.scrumly.userservice.userservice.dto.service.invite.InviteDto;
import com.scrumly.userservice.userservice.enums.TeamMemberRole;
import com.scrumly.userservice.userservice.enums.members.MemberStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberDto {
    private Long id;
    private TeamMemberRole role;
    private LocalDateTime joinDateTime;
    private MemberStatus status;
    private String badge;
    private UserProfileDto profile;
    private List<MemberHistoryDto> changeHistory;
}
