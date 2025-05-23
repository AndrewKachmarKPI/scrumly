package com.scrumly.eventservice.api;

import com.scrumly.eventservice.dto.ActivityTemplateDto;
import com.scrumly.eventservice.dto.ActivityTemplateGroupDto;
import com.scrumly.eventservice.dto.requests.CreateActivityTemplateRQ;
import com.scrumly.eventservice.services.ActivityTemplateService;
import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/activity-templates")
@Validated
@RequiredArgsConstructor
public class ActivityTemplateController {

    private final ActivityTemplateService activityTemplateService;


    @PostMapping
    public ResponseEntity<ActivityTemplateDto> createActivityTemplate(
            @Valid @RequestPart("createRQ") CreateActivityTemplateRQ createRQ,
            @RequestPart(value = "previewImg", required = false) MultipartFile previewImg) {
        ActivityTemplateDto result = (previewImg != null) ?
                activityTemplateService.createActivityTemplate(createRQ, previewImg) :
                activityTemplateService.createActivityTemplate(createRQ);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{templateId}")
    public ResponseEntity<ActivityTemplateDto> updateActivityTemplate(
            @PathVariable @NotBlank String templateId,
            @Valid @RequestPart("templateRQ") ActivityTemplateDto templateRQ,
            @RequestParam(value = "previewImg", required = false) MultipartFile previewImg) {
        ActivityTemplateDto result = (previewImg != null) ?
                activityTemplateService.updateActivityTemplate(templateId, templateRQ, previewImg) :
                activityTemplateService.updateActivityTemplate(templateId, templateRQ);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{templateId}/copy")
    public ResponseEntity<ActivityTemplateDto> copyActivityTemplate(
            @PathVariable @NotBlank String templateId,
            @RequestParam @NotBlank String ownerId) {
        ActivityTemplateDto result = activityTemplateService.copyActivityTemplate(templateId, ownerId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/search")
    public ResponseEntity<PageDto<ActivityTemplateDto>> findActivityTemplates(
            @Valid @RequestBody SearchQuery searchQuery) {
        return ResponseEntity.ok(activityTemplateService.findActivityTemplates(searchQuery));
    }

    @PostMapping("/search/info")
    public ResponseEntity<PageDto<ActivityTemplateDto>> findActivityTemplatesInfo(
            @Valid @RequestBody SearchQuery searchQuery) {
        return ResponseEntity.ok(activityTemplateService.findActivityTemplatesInfo(searchQuery));
    }

    @PostMapping("/search/my")
    public ResponseEntity<PageDto<ActivityTemplateDto>> findMyActivityTemplates(
            @Valid @RequestBody SearchQuery searchQuery) {
        return ResponseEntity.ok(activityTemplateService.findMyActivityTemplates(searchQuery));
    }

    @GetMapping("/my/group")
    public ResponseEntity<List<ActivityTemplateGroupDto>> findMyActivityTemplates(@RequestParam(required = false) String ownerId) {
        return ResponseEntity.ok(activityTemplateService.findMyActivityTemplatesGroups(ownerId));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<ActivityTemplateDto> findActivityTemplateById(
            @PathVariable @NotBlank String templateId) {
        return ResponseEntity.ok(activityTemplateService.findActivityTemplateById(templateId));
    }

    @GetMapping("/{templateId}/owner/{ownerId}")
    public ResponseEntity<ActivityTemplateDto> findActivityTemplateByIdAndOwner(
            @PathVariable @NotBlank String templateId,
            @PathVariable @NotBlank String ownerId) {
        return ResponseEntity.ok(activityTemplateService.findActivityTemplateByIdAndOwner(templateId, ownerId));
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteActivityTemplate(
            @PathVariable @NotBlank String templateId,
            @RequestParam @NotBlank String ownerId) {
        activityTemplateService.deleteActivityTemplate(templateId, ownerId);
        return ResponseEntity.noContent().build();
    }
}
