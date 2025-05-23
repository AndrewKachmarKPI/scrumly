package com.scrumly.feign;

import com.scrumly.config.FeignClientConfig;
import com.scrumly.dto.ImageRequestDto;
import com.scrumly.dto.user.TeamMetadataDto;
import com.scrumly.dto.user.UserProfileDto;
import com.scrumly.enums.userservice.PermissionType;
import com.scrumly.validations.SqlInjectionSafe;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

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

    @GetMapping("/username/{username}")
    ResponseEntity<UserProfileDto> findUserProfile(@PathVariable("username")
                                                   @NotNull @NotEmpty @NotBlank
                                                   @SqlInjectionSafe String username);
    @PostMapping("/users")
    List<UserProfileDto> findUsers(@RequestBody Set<String> userIds);

    @GetMapping("/team/{teamId}/users")
    List<UserProfileDto> findTeamUsers(@PathVariable("teamId") String teamId);

    @PostMapping("/permissions")
    boolean hasPermission(@RequestParam("permission") PermissionType permissionType,
                          @RequestBody List<String> params);

    @GetMapping("/team/{teamId}/metadata")
    TeamMetadataDto findTeamMetadata(@PathVariable("teamId") String teamId);
}
