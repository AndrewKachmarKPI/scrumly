package com.scrumly.userservice.userservice.api;

import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import com.scrumly.userservice.userservice.dto.requests.CreateTeamRQ;
import com.scrumly.userservice.userservice.dto.requests.UpdateTeamRQ;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationTeamGroupDto;
import com.scrumly.userservice.userservice.dto.service.team.TeamDto;
import com.scrumly.userservice.userservice.dto.service.team.TeamMetadataDto;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import com.scrumly.userservice.userservice.services.TeamService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.scrumly.userservice.userservice.utils.SecurityUtils.getUsername;

@CrossOrigin
@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
@Validated
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamDto> createTeam(@Valid @RequestBody CreateTeamRQ createTeamRQ) {
        TeamDto team = teamService.createTeam(createTeamRQ);
        return ResponseEntity.status(HttpStatus.CREATED).body(team);
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<TeamDto> updateTeam(@PathVariable @NotNull String teamId,
                                              @Valid @RequestBody UpdateTeamRQ updateTeamRQ) {
        TeamDto updatedTeam = teamService.updateTeam(teamId, updateTeamRQ);
        return ResponseEntity.ok(updatedTeam);
    }

    @PostMapping("/all")
    public ResponseEntity<PageDto<TeamDto>> findTeams(@RequestBody SearchQuery searchQuery) {
        PageDto<TeamDto> teams = teamService.findTeams(searchQuery);
        return ResponseEntity.ok(teams);
    }

    @PostMapping("/me")
    public ResponseEntity<PageDto<TeamDto>> findMyTeams(@RequestBody SearchQuery searchQuery) {
        PageDto<TeamDto> teams = teamService.findMyTeams(searchQuery);
        return ResponseEntity.ok(teams);
    }

    @PostMapping("/organization/{orgId}")
    public ResponseEntity<PageDto<TeamDto>> findTeamsByOrganizationId(@PathVariable @NotNull String orgId,
                                                                      @Valid @RequestBody SearchQuery searchQuery) {
        PageDto<TeamDto> teams = teamService.findTeamsByOrganizationId(searchQuery, orgId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamDto> findTeamById(@PathVariable @NotNull String teamId) {
        TeamDto team = teamService.findTeamById(teamId);
        return ResponseEntity.ok(team);
    }

    @GetMapping("/{teamId}/metadata")
    public ResponseEntity<TeamMetadataDto> findTeamMetadataById(@PathVariable @NotNull String teamId) {
        return ResponseEntity.ok(teamService.findTeamMetadata(teamId));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeamById(@PathVariable @NotNull String teamId) {
        teamService.deleteTeamById(teamId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{teamId}/invite")
    public ResponseEntity<TeamDto> inviteTeamMembers(@PathVariable @NotNull String teamId,
                                                     @RequestBody List<String> inviteMembers) {
        TeamDto team = teamService.inviteTeamMembers(teamId, inviteMembers);
        return ResponseEntity.ok(team);
    }

    @PostMapping("/{teamId}/remove")
    public ResponseEntity<TeamDto> removeTeamMembers(@PathVariable String teamId,
                                                     @RequestBody List<String> members) {
        TeamDto team = teamService.removeTeamMembers(teamId, members);
        return ResponseEntity.ok(team);
    }

    @PostMapping("/{teamId}/check")
    public ResponseEntity<Boolean> checkIfExistsInTeam(@PathVariable String teamId,
                                                       @RequestBody List<String> userIds) {
        boolean exists = teamService.checkIfExistsInTeam(teamId, userIds);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/groups/me")
    public ResponseEntity<List<OrganizationTeamGroupDto>> findOrganizationTeamGroup() {
        return ResponseEntity.ok(teamService.findOrganizationTeamGroup(getUsername()));
    }


    @GetMapping("/{teamId}/users")
    public ResponseEntity<List<UserProfileDto>> findTeamUsers(@PathVariable("teamId") String teamId) {
        return ResponseEntity.ok(teamService.findTeamUsers(teamId));
    }
}
