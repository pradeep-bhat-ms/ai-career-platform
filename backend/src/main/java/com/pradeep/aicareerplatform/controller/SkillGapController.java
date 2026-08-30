package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.SkillGapOverviewDto;
import com.pradeep.aicareerplatform.service.SkillGapService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skill-gap")
public class SkillGapController {

    private final SkillGapService skillGapService;

    public SkillGapController(SkillGapService skillGapService) {
        this.skillGapService = skillGapService;
    }

    @GetMapping
    public ResponseEntity<SkillGapOverviewDto> getSkillGap(
            @RequestParam Long resumeId,
            @RequestParam String targetRole,
            Authentication authentication) throws Exception {

        String userEmail = authentication.getName();
        SkillGapOverviewDto response = skillGapService.computeSkillGap(resumeId, targetRole, userEmail);
        return ResponseEntity.ok(response);
    }
}