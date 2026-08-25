package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.JobDescriptionResponseDto;
import com.pradeep.aicareerplatform.dto.JobDescriptionSubmitRequestDto;
import com.pradeep.aicareerplatform.service.JobDescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job-description")
public class JobDescriptionController {

    private final JobDescriptionService jobDescriptionService;

    public JobDescriptionController(JobDescriptionService jobDescriptionService) {
        this.jobDescriptionService = jobDescriptionService;
    }

    @PostMapping("/submit")
    public ResponseEntity<JobDescriptionResponseDto> submitAndAnalyze(
            @Valid @RequestBody JobDescriptionSubmitRequestDto request,
            Authentication authentication) throws Exception {

        String userEmail = authentication.getName();
        JobDescriptionResponseDto response = jobDescriptionService.submitAndAnalyze(request, userEmail);
        return ResponseEntity.ok(response);
    }
}