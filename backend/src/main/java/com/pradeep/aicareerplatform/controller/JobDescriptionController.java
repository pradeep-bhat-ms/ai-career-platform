package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.JobDescriptionResponseDto;
import com.pradeep.aicareerplatform.dto.JobDescriptionSubmitRequestDto;
import com.pradeep.aicareerplatform.entity.JobDescription;
import com.pradeep.aicareerplatform.service.JobDescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping("/my-job-descriptions")
    public ResponseEntity<List<JobDescription>> getMyJobDescriptions(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(jobDescriptionService.getJobDescriptionsForUser(userEmail));
    }
    @DeleteMapping("/{jobDescriptionId}")
    public ResponseEntity<Void> deleteJobDescription(
            @PathVariable Long jobDescriptionId,
            Authentication authentication) {

        String userEmail = authentication.getName();
        jobDescriptionService.deleteJobDescription(jobDescriptionId, userEmail);
        return ResponseEntity.noContent().build();
    }
}