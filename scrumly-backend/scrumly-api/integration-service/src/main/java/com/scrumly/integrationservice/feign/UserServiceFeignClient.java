package com.scrumly.integrationservice.feign;

import com.scrumly.integrationservice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "user-service", configuration = FeignClientConfig.class)
public interface UserServiceFeignClient {
}
