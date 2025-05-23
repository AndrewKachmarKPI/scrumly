package com.scrumly.api;

import com.scrumly.dto.issues.IssueShortInfo;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.service.IssueService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssuesController {
    private final IssueService issueService;


    @GetMapping("/search")
    public ResponseEntity<List<IssueShortInfo>> searchIssues(@RequestParam @NotNull ServiceType serviceType,
                                                             @RequestParam @NotBlank String connectingId,
                                                             @RequestParam @NotBlank String query) {
        return ResponseEntity.ok(issueService.searchIssues(serviceType, connectingId, query));
    }

    @GetMapping("/top")
    public ResponseEntity<List<IssueShortInfo>> loadTopIssues(@RequestParam @NotNull ServiceType serviceType,
                                                             @RequestParam @NotBlank String connectingId,
                                                             @RequestParam @NotNull Integer topLimit) {
        return ResponseEntity.ok(issueService.loadTopIssues(serviceType, connectingId, topLimit));
    }
}
