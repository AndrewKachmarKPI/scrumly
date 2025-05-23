package com.scrumly.feign;

import com.scrumly.config.FeignClientConfig;
import com.scrumly.event.dto.activity.ActivityDto;
import com.scrumly.event.enums.ActivityStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "event-service", configuration = FeignClientConfig.class)
public interface EventServiceFeignClient {

    @GetMapping("/api/activities/{activityId}")
    ActivityDto getActivityById(@PathVariable String activityId);

    @PostMapping("/api/activities/{activityId}/ref")
    void saveRoomReference(@PathVariable String activityId, @RequestParam String roomId);

    @PutMapping("/api/activities/{activityId}/status")
    ActivityDto changeActivityStatus(@PathVariable("activityId") @NotNull String activityId,
                                     @Valid @RequestBody ActivityStatus activityStatus);

    @GetMapping("/api/activities/team/{teamId}/template/{templateId}")
    List<ActivityDto> getRecentTemplateActivities(@PathVariable @NotNull String teamId,
                                                  @PathVariable @NotNull String templateId);
}
