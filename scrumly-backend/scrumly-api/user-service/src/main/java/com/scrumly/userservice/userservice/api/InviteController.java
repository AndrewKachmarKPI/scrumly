package com.scrumly.userservice.userservice.api;

import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import com.scrumly.userservice.userservice.dto.service.invite.InviteDto;
import com.scrumly.userservice.userservice.services.OrganizationInviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.scrumly.userservice.userservice.utils.SecurityUtils.getUsername;

@CrossOrigin
@RestController
@RequestMapping("/invites")
@RequiredArgsConstructor
@Validated
public class InviteController {
    private final OrganizationInviteService organizationInviteService;

    @PostMapping("/org/me")
    public ResponseEntity<PageDto<InviteDto>> findMyOrganizationInvites(SearchQuery searchQuery) {
        return ResponseEntity.ok(organizationInviteService.findUserOrganizationInvites(getUsername(), searchQuery));
    }
}
