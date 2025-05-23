package com.scrumly.eventservice.api;

import com.scrumly.eventservice.dto.workspace.WorkspaceDto;
import com.scrumly.eventservice.services.WorkspaceService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@CrossOrigin
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceDto> findWorkspace(@PathVariable("workspaceId") @NotNull @NotBlank String workspaceId) {
        return ResponseEntity.ok(workspaceService.findWorkspace(workspaceId));
    }

    @PutMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceDto> createWorkspaceConference(@PathVariable("workspaceId") @NotNull @NotBlank String workspaceId) {
        return ResponseEntity.ok(workspaceService.createWorkspaceConference(workspaceId));
    }
}
