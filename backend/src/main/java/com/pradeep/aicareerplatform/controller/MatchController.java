package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.JdMatchResponseDto;
import com.pradeep.aicareerplatform.service.JdMatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    private final JdMatchService jdMatchService;

    public MatchController(JdMatchService jdMatchService) {
        this.jdMatchService = jdMatchService;
    }

    @GetMapping("/{resumeId}/{jobDescriptionId}")
    public ResponseEntity<JdMatchResponseDto> matchResumeToJob(
            @PathVariable Long resumeId,
            @PathVariable Long jobDescriptionId,
            Authentication authentication) throws Exception {

        String userEmail = authentication.getName();
        JdMatchResponseDto response = jdMatchService.matchResumeToJob(resumeId, jobDescriptionId, userEmail);
        return ResponseEntity.ok(response);
    }
}