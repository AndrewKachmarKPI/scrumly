package com.scrumly.userservice.userservice.api;

import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import com.scrumly.userservice.userservice.dto.requests.InviteMembersRQ;
import com.scrumly.userservice.userservice.dto.service.invite.InviteDto;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationMemberDto;
import com.scrumly.userservice.userservice.services.InviteService;
import com.scrumly.userservice.userservice.services.OrganizationInviteService;
import com.scrumly.userservice.userservice.services.OrganizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.scrumly.userservice.userservice.utils.SecurityUtils.getUsername;

@CrossOrigin
@RestController
@RequestMapping("/org/{orgId}/invites")
@RequiredArgsConstructor
@Validated
public class OrganizationInvitesController {
    private final OrganizationService organizationService;
    private final OrganizationInviteService organizationInviteService;

    @PostMapping("/all")
    public ResponseEntity<PageDto<InviteDto>> findOrganizationInvites(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                                                      @Valid @RequestBody SearchQuery searchQuery) {
        return ResponseEntity.ok(organizationInviteService.findOrganizationInvites(orgId, searchQuery));
    }

    @PutMapping("/invite")
    public ResponseEntity<List<String>> inviteOrganizationMembers(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                                          @Valid @RequestBody InviteMembersRQ inviteMembersRQ) {
        organizationInviteService.inviteOrganizationMembers(orgId, inviteMembersRQ);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{inviteId}")
    public ResponseEntity<InviteDto> findInvite(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                                @PathVariable("inviteId") @NotNull @NotBlank String inviteId) {
        return ResponseEntity.ok(organizationInviteService.findInvite(orgId, inviteId));
    }

    @PostMapping("/{inviteId}/resend")
    public ResponseEntity<InviteDto> resendInvite(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                                  @PathVariable("inviteId") @NotNull @NotBlank String inviteId) {
        organizationInviteService.resendInvite(orgId, inviteId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{inviteId}/accept")
    public ResponseEntity<InviteDto> acceptInvite(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                                  @PathVariable("inviteId") @NotNull @NotBlank String inviteId) {
        organizationInviteService.acceptInvite(orgId, inviteId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{inviteId}/reject")
    public ResponseEntity<InviteDto> rejectInvite(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                                  @PathVariable("inviteId") @NotNull @NotBlank String inviteId) {
        organizationInviteService.rejectInvite(orgId, inviteId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{inviteId}/revoke")
    public ResponseEntity<InviteDto> revokeInvite(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                                  @PathVariable("inviteId") @NotNull @NotBlank String inviteId) {
        organizationInviteService.revokeInvite(orgId, inviteId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{inviteId}/delete")
    public ResponseEntity<InviteDto> deleteInvite(@PathVariable("orgId") @NotNull @NotBlank String orgId,
                                                  @PathVariable("inviteId") @NotNull @NotBlank String inviteId) {
        organizationInviteService.deleteInvite(orgId, inviteId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/exists")
    public ResponseEntity<Boolean> checkIfExistsInOrganization(@PathVariable("orgId") String orgId,
                                                               @RequestBody List<String> userIds) {
        return ResponseEntity.ok(organizationService.checkIfExistsInOrganization(orgId, userIds));
    }
}
