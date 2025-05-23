package com.scrumly.integrationservice.api;

import com.scrumly.enums.integration.ServiceType;
import com.scrumly.integrationservice.dto.IntegrationServiceDto;
import com.scrumly.integrationservice.dto.ServiceRefreshRQ;
import com.scrumly.integrationservice.enums.IntegrationServiceScope;
import com.scrumly.integrationservice.service.IntegrationService;
import com.scrumly.integrationservice.service.ServiceCredentialsService;
import com.scrumly.validations.SqlInjectionSafe;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.scrumly.integrationservice.utils.SecurityUtils.getUsername;

@CrossOrigin
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Validated
public class IntegrationServiceController {
    private final IntegrationService integrationService;
    private final ServiceCredentialsService credentialsService;

    @GetMapping("/me")
    public ResponseEntity<List<IntegrationServiceDto>> findMyIntegrationServices() {
        return ResponseEntity.ok(integrationService.findIntegrationServices(getUsername(), IntegrationServiceScope.MEMBER));
    }

    @GetMapping("/is-connected/user")
    public ResponseEntity<Boolean> isConnectedUser(@RequestParam("serviceType") ServiceType serviceType) {
        return ResponseEntity.ok(integrationService.isConnected(serviceType));
    }

    @GetMapping("/is-connected")
    public ResponseEntity<Boolean> isConnected(@RequestParam("serviceType") ServiceType serviceType,
                                               @RequestParam("connectionId") String connectionId) {
        return ResponseEntity.ok(integrationService.isConnected(serviceType, connectionId));
    }
    @GetMapping("/connected")
    public ResponseEntity<List<ServiceType>> findConnectedServices(@RequestParam("connectionId") String connectionId) {
        return ResponseEntity.ok(integrationService.findConnectedServices(connectionId));
    }

    @GetMapping("/org/{orgId}")
    public ResponseEntity<List<IntegrationServiceDto>> findOrgIntegrationServices(@PathVariable("orgId") @NotNull @NotBlank String orgId) {
        return ResponseEntity.ok(integrationService.findIntegrationServices(orgId, IntegrationServiceScope.ORGANIZATION));
    }

    @DeleteMapping
    public ResponseEntity<Void> disconnectService(@RequestParam("serviceName") @NotNull @NotBlank @SqlInjectionSafe String serviceName,
                                                  @RequestParam(value = "connectionId", required = false) String connectionId) {
        String serviceId = connectionId != null && !connectionId.isBlank()
                ? connectionId
                : getUsername();
        integrationService.disconnect(serviceId, ServiceType.valueOf(serviceName));
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<Void> refreshServiceAccess(@Valid @RequestBody ServiceRefreshRQ refreshRQ) {
        integrationService.refresh(refreshRQ);
        return ResponseEntity.noContent().build();
    }
}
