package com.scrumly.userservice.userservice.feign;

import com.scrumly.userservice.userservice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "integration-service", configuration = FeignClientConfig.class)
public interface IntegrationServiceFeignClient {
}
