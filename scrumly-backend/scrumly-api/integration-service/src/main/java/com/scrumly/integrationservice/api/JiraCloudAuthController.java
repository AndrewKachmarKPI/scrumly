package com.scrumly.integrationservice.api;

import com.scrumly.enums.integration.ServiceType;
import com.scrumly.integrationservice.dto.ServiceAuthorizeRQ;
import com.scrumly.integrationservice.service.IntegrationService;
import com.scrumly.integrationservice.service.jira.JiraCloudApiService;
import com.scrumly.integrationservice.service.jira.JiraCloudAuthService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/jira/cloud")
@RequiredArgsConstructor
@Validated
public class JiraCloudAuthController {
    private final JiraCloudAuthService jiraCloudAuthService;
    private final IntegrationService integrationService;


    @GetMapping("/authorize")
    public String getAuthorizationUrl() {
        return jiraCloudAuthService.getAuthorizationUrl();
    }

    @PostMapping("/authorize")
    public ResponseEntity<Void> authorize(@RequestParam(value = "code") @NotNull String code,
                                          @RequestParam(value = "orgId") @NotNull String orgId) {
        integrationService.authorize(ServiceAuthorizeRQ.builder()
                .code(code)
                .connectingId(orgId)
                .serviceType(ServiceType.JIRA_CLOUD)
                .build());
        return ResponseEntity.ok().build();
    }
}
