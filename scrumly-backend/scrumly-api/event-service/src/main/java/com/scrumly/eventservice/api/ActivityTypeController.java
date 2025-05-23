package com.scrumly.eventservice.api;

import com.scrumly.eventservice.dto.ActivityTypeDto;
import com.scrumly.eventservice.services.ActivityTypeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activity-types")
@Validated
@RequiredArgsConstructor
public class ActivityTypeController {

    private final ActivityTypeService service;

    @PostMapping
    public ResponseEntity<ActivityTypeDto> create(@Valid @RequestBody ActivityTypeDto dto) {
        ActivityTypeDto createdDto = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityTypeDto> update(@PathVariable @NotNull Long id,
                                                  @Valid @RequestBody ActivityTypeDto dto) {
        ActivityTypeDto updatedDto = service.update(id, dto);
        return ResponseEntity.ok(updatedDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityTypeDto> getById(@PathVariable Long id) {
        ActivityTypeDto dto = service.getById(id);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ActivityTypeDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
