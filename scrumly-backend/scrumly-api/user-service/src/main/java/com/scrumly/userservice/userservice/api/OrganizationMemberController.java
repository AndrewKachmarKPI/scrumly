package com.scrumly.userservice.userservice.api;

import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationMemberDto;
import com.scrumly.userservice.userservice.enums.organization.OrganizationMemberRole;
import com.scrumly.userservice.userservice.services.OrganizationMemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.scrumly.userservice.userservice.utils.SecurityUtils.getUsername;

@CrossOrigin
@RestController
@RequestMapping("/org/{orgId}/members")
@RequiredArgsConstructor
@Validated
public class OrganizationMemberController {
    private final OrganizationMemberService organizationMemberService;

    @GetMapping("/me")
    public ResponseEntity<OrganizationMemberDto> findMyOrganizationMemberAccount(@PathVariable("orgId") @NotNull @NotBlank String orgId) {
        return ResponseEntity.ok(organizationMemberService.findMyOrganizationMemberAccount(orgId, getUsername()));
    }

    @PostMapping
    public ResponseEntity<PageDto<OrganizationMemberDto>> findOrganizationMembers(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                                                                  @Valid @RequestBody SearchQuery searchQuery) {
        return ResponseEntity.ok(organizationMemberService.findOrganizationMembers(orgId, searchQuery));
    }

    @PutMapping("/{username}/role")
    public ResponseEntity<OrganizationMemberDto> changeMemberRole(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                                                  @PathVariable("username") @NotNull @NotBlank String username,
                                                                  @RequestParam("role") @NotNull OrganizationMemberRole memberRole) {
        return ResponseEntity.ok(organizationMemberService.changeMemberRole(orgId, username, memberRole));
    }

    @PutMapping("/{username}/block")
    public ResponseEntity<OrganizationMemberDto> blockMember(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                                             @PathVariable("username") @NotNull @NotBlank String username) {
        return ResponseEntity.ok(organizationMemberService.blockMember(orgId, username));
    }

    @PutMapping("/{username}/activate")
    public ResponseEntity<OrganizationMemberDto> activateMember(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                                                @PathVariable("username") @NotNull @NotBlank String username) {
        return ResponseEntity.ok(organizationMemberService.activateMember(orgId, username));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> removeMember(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                             @PathVariable("username") @NotNull @NotBlank String username) {
        organizationMemberService.removeMember(orgId, username);
        return ResponseEntity.noContent().build();
    }
}
