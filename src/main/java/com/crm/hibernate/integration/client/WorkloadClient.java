package com.crm.hibernate.integration.client;

import com.crm.hibernate.integration.security.WorkloadFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Resolved through Eureka by service name; wrapped by a Resilience4j circuit breaker. */
@FeignClient(name = "trainer-workload-service",
        configuration = WorkloadFeignConfig.class,
        fallbackFactory = WorkloadClientFallbackFactory.class)

public interface WorkloadClient {

    @PostMapping("/api/trainers/workloads")
    void updateWorkload(@RequestBody TrainerWorkloadRequest request);

    @GetMapping("/api/trainers/workloads/{username}")
    Map<String, Object> getWorkload(@PathVariable("username") String username);

    @GetMapping("/api/trainers/workloads/{username}/months")
    Map<String, Object> getMonthlyHours(
            @PathVariable("username") String username,
            @RequestParam("year") int year,
            @RequestParam("month") int month);
}