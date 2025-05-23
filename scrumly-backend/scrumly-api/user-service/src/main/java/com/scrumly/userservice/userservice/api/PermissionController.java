package com.scrumly.userservice.userservice.api;

import com.scrumly.enums.userservice.PermissionType;
import com.scrumly.userservice.userservice.services.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.scrumly.userservice.userservice.utils.SecurityUtils.getUsername;

@CrossOrigin
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Validated
public class PermissionController {
    private final PermissionService permissionService;


    @PostMapping
    public ResponseEntity<Boolean> hasPermission(@RequestParam("permission") PermissionType permissionType,
                                                 @RequestBody List<String> params) {
        return ResponseEntity.ok(permissionService.hasPermission(permissionType, getUsername(), params));
    }
}
