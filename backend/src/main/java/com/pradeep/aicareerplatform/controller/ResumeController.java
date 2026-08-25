package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.config.RoleSkillConfig;
import com.pradeep.aicareerplatform.dto.CareerSkillAgentResponseDto;
import com.pradeep.aicareerplatform.dto.ResumeAnalysisResponseDto;
import com.pradeep.aicareerplatform.dto.ResumeUploadResponseDto;

import com.pradeep.aicareerplatform.dto.RoleAnalysisResponseDto;
import com.pradeep.aicareerplatform.service.ResumeService;
import com.pradeep.aicareerplatform.service.RoleAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final RoleAnalysisService roleAnalysisService;
    private final ResumeService resumeService;
    private final RoleSkillConfig roleSkillConfig;

    public ResumeController(ResumeService resumeService , RoleAnalysisService roleAnalysisService , RoleSkillConfig roleSkillConfig) {
        this.resumeService = resumeService;
        this.roleAnalysisService = roleAnalysisService;
        this.roleSkillConfig = roleSkillConfig;
    }

    @PostMapping("/{resumeId}/analyze")
    public ResponseEntity<ResumeAnalysisResponseDto> analyzeResume(
            @PathVariable Long resumeId,
            Authentication authentication) throws Exception {

        String userEmail = authentication.getName();
        ResumeAnalysisResponseDto response = resumeService.analyzeResume(resumeId, userEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    public ResponseEntity<ResumeUploadResponseDto> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        String userEmail = authentication.getName();
        ResumeUploadResponseDto response = resumeService.uploadResume(file, userEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{resumeId}/analyze-for-role")
    public ResponseEntity<RoleAnalysisResponseDto> analyzeForRole(
            @PathVariable Long resumeId,
            @RequestParam String targetRole,
            Authentication authentication) throws Exception {

        String userEmail = authentication.getName();

        RoleAnalysisResponseDto response =
                roleAnalysisService.analyzeForRole(
                        resumeId,
                        targetRole,
                        userEmail
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/available-roles")
    public ResponseEntity<List<String>> getAvailableRoles() {
        return ResponseEntity.ok(roleSkillConfig.getAvailableRoles());
    }

    @PostMapping("/{resumeId}/career-agent")
    public ResponseEntity<CareerSkillAgentResponseDto> runCareerAgent(
            @PathVariable Long resumeId,
            @RequestParam String targetRole,
            Authentication authentication) throws Exception {

        String userEmail = authentication.getName();
        CareerSkillAgentResponseDto response = resumeService.runCareerSkillAgent(resumeId, targetRole, userEmail);
        return ResponseEntity.ok(response);
    }
}