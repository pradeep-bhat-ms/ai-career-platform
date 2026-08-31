package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.DashboardSummaryDto;
import com.pradeep.aicareerplatform.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(dashboardService.getSummary(userEmail));
    }
}