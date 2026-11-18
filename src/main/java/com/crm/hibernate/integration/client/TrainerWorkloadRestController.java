//package com.crm.hibernate.integration.client;
//
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/trainers")
//public class TrainerWorkloadRestController {
//
//    private final WorkloadClient workloadClient;
//
//    public TrainerWorkloadRestController(WorkloadClient workloadClient) {
//        this.workloadClient = workloadClient;
//    }
//
//    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINER') and #username == authentication.name)")
//    @GetMapping("/{username}/workload")
//    public Map<String, Object> getWorkload(@PathVariable("username") String username) {
//        return workloadClient.getWorkload(username);
//    }
//
//    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINER') and #username == authentication.name)")
//    @GetMapping("/{username}/workload/months")
//    public Map<String, Object> getMonthlyHours(@PathVariable("username") String username,
//                                               @RequestParam("year") int year,
//                                               @RequestParam("month") int month) {
//        return workloadClient.getMonthlyHours(username, year, month);
//    }
//}