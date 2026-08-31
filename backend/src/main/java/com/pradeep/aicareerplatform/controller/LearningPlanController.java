package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.LearningPlanResponseDto;
import com.pradeep.aicareerplatform.service.LearningPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning-plan")
public class LearningPlanController {

    private final LearningPlanService learningPlanService;

    public LearningPlanController(LearningPlanService learningPlanService) {
        this.learningPlanService = learningPlanService;
    }

    @PostMapping("/generate")
    public ResponseEntity<LearningPlanResponseDto> generatePlan(
            @RequestParam Long resumeId,
            @RequestParam String targetRole,
            Authentication authentication) throws Exception {

        String userEmail = authentication.getName();
        LearningPlanResponseDto response = learningPlanService.generatePlan(resumeId, targetRole, userEmail);
        return ResponseEntity.ok(response);
    }
}