package com.scrumly.eventservice.feign;

import com.scrumly.dto.ImageRequestDto;
import com.scrumly.enums.userservice.PermissionType;
import com.scrumly.eventservice.config.FeignClientConfig;
import com.scrumly.eventservice.dto.user.TeamMetadataDto;
import com.scrumly.eventservice.dto.user.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "user-service", configuration = FeignClientConfig.class)
public interface UserServiceFeignClient {
    @PostMapping("/assets/parsed")
    ResponseEntity<String> saveImage(@RequestBody ImageRequestDto dto);

    @PutMapping("/assets/{imageId}")
    ResponseEntity<String> copyImage(@PathVariable("imageId") String imageId);

    @DeleteMapping("/assets/{imageId}")
    ResponseEntity<Void> deleteImageById(@PathVariable("imageId") String imageId);

    @GetMapping("/me")
    ResponseEntity<UserProfileDto> findMyUserProfile();

    @PostMapping("/permissions")
    boolean hasPermission(@RequestParam("permission") PermissionType permissionType,
                          @RequestBody List<String> params);

    @GetMapping("/team/{teamId}/metadata")
    TeamMetadataDto findTeamMetadata(@PathVariable("teamId") String teamId);
}
