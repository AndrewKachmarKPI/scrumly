package com.scrumly.userservice.userservice.api;

import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import com.scrumly.userservice.userservice.dto.requests.CreateOrganizationRQ;
import com.scrumly.userservice.userservice.dto.requests.CreateTeamRQ;
import com.scrumly.userservice.userservice.dto.requests.OrganizationInfoRQ;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationDto;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationInfoDto;
import com.scrumly.userservice.userservice.services.OrganizationService;
import com.scrumly.userservice.userservice.services.TeamService;
import com.scrumly.userservice.userservice.utils.ValidFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.scrumly.userservice.userservice.utils.SecurityUtils.getUsername;

@CrossOrigin
@RestController
@RequestMapping("/org")
@RequiredArgsConstructor
@Validated
public class OrganizationController {
    private final OrganizationService organizationService;
    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<OrganizationInfoDto> createOrganization(@RequestPart("rq") @Valid CreateOrganizationRQ rq,
                                                                  @RequestPart(value = "logo", required = false) @ValidFile MultipartFile logo) {
        OrganizationInfoDto organizationInfoDto = organizationService.createOrganization(rq, logo);
        if (rq.getTeamName() != null && !rq.getTeamName().isBlank()) {
            teamService.createTeam(CreateTeamRQ.builder()
                                           .teamName(rq.getTeamName())
                                           .organizationId(organizationInfoDto.getOrganizationId())
                                           .build());
        }
        return ResponseEntity.ok(organizationInfoDto);
    }

    @PutMapping("/{orgId}")
    public ResponseEntity<OrganizationInfoDto> updateOrganization(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                                                  @RequestPart("rq") @Valid OrganizationInfoRQ rq,
                                                                  @RequestPart(value = "logo", required = false) @ValidFile MultipartFile logo) {
        return ResponseEntity.ok(organizationService.updateOrganization(orgId, rq, logo));
    }


    @PostMapping("/me")
    public ResponseEntity<PageDto<OrganizationInfoDto>> findUserOrganizations(@Valid @RequestBody SearchQuery searchQuery) {
        return ResponseEntity.ok(organizationService.findOrganizationsByUsername(searchQuery, getUsername()));
    }

    @PostMapping("/user/{username}")
    public ResponseEntity<PageDto<OrganizationInfoDto>> findUserOrganizations(@Valid @RequestBody SearchQuery searchQuery,
                                                                              @PathVariable("username") String username) {
        return ResponseEntity.ok(organizationService.findOrganizationsByUsername(searchQuery, username));
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<OrganizationDto> findOrganizationById(@PathVariable("orgId") @NotNull @NotBlank String orgId) {
        return ResponseEntity.ok(organizationService.findOrganization(orgId));
    }

    @GetMapping("/{orgId}/info")
    public ResponseEntity<OrganizationInfoDto> findOrganizationInfo(@PathVariable("orgId") @NotNull @NotBlank String orgId) {
        return ResponseEntity.ok(organizationService.findOrganizationInfo(orgId));
    }

    @PostMapping("/info")
    public ResponseEntity<List<OrganizationInfoDto>> findOrganizationInfoList(@RequestBody @NotNull @NotEmpty List<String> orgIds) {
        return ResponseEntity.ok(organizationService.findOrganizationInfoList(orgIds));
    }

    @DeleteMapping("/{orgId}/archive")
    public ResponseEntity<OrganizationInfoDto> archiveOrganization(@PathVariable("orgId") @NotNull @NotBlank String orgId) {
        return ResponseEntity.ok(organizationService.archiveOrganization(orgId));
    }

    @DeleteMapping("/{orgId}/delete")
    public ResponseEntity<OrganizationInfoDto> deleteOrganization(@PathVariable("orgId") @NotNull @NotBlank String orgId) {
        return ResponseEntity.ok(organizationService.deleteOrganization(orgId));
    }
}
