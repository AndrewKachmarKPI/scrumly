package com.scrumly.userservice.userservice.services.impls;

import com.scrumly.enums.userservice.PermissionType;
import com.scrumly.userservice.userservice.domain.team.TeamEntity;
import com.scrumly.userservice.userservice.dto.service.team.TeamDto;
import com.scrumly.userservice.userservice.enums.TeamMemberRole;
import com.scrumly.userservice.userservice.services.OrganizationService;
import com.scrumly.userservice.userservice.services.PermissionService;
import com.scrumly.userservice.userservice.services.TeamService;
import com.scrumly.userservice.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final TeamService teamService;

    @Override
    public boolean hasPermission(PermissionType permission, String username, List<String> params) {
        try {
            if (PermissionType.SCHEDULE_ACTIVITY.equals(permission)) {
                String teamId = params.get(0);
                TeamDto team = teamService.findTeamById(teamId);
                return team.getMembers().stream()
                        .anyMatch(teamMemberDto -> teamMemberDto.getProfile().getUserId().equals(username) &&
                                !teamMemberDto.getRole().equals(TeamMemberRole.MEMBER));
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
