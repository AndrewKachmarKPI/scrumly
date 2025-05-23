package com.scrumly.conferenceservice.feign;

import com.scrumly.conferenceservice.config.FeignClientConfig;
import com.scrumly.conferenceservice.dto.user.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@FeignClient(value = "user-service", configuration = FeignClientConfig.class)
public interface UserServiceFeignClient {
    @GetMapping("/me")
    ResponseEntity<UserProfileDto> findMyUserProfile();

    @PostMapping("/users")
    ResponseEntity<List<UserProfileDto>> findUsers(@RequestBody Set<String> userIds);

    @PostMapping("/permissions")
    boolean hasPermission(@RequestParam("permission") String permissionType,
                          @RequestBody List<String> params);
}
