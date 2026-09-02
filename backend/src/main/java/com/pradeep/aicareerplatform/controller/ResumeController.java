package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.config.RoleSkillConfig;
import com.pradeep.aicareerplatform.dto.*;
import com.pradeep.aicareerplatform.entity.Resume;
import com.pradeep.aicareerplatform.service.ResumeImprovementService;
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
    private final ResumeImprovementService resumeImprovementService;

    public ResumeController(ResumeService resumeService,
                            RoleAnalysisService roleAnalysisService,
                            RoleSkillConfig roleSkillConfig,
                            ResumeImprovementService resumeImprovementService) {
        this.resumeService = resumeService;
        this.roleAnalysisService = roleAnalysisService;
        this.roleSkillConfig = roleSkillConfig;
        this.resumeImprovementService = resumeImprovementService;
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

    @GetMapping("/my-resumes")
    public ResponseEntity<List<Resume>> getMyResumes(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(resumeService.getResumesForUser(userEmail));
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long resumeId,
            Authentication authentication) {

        String userEmail = authentication.getName();
        resumeService.deleteResume(resumeId, userEmail);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{resumeId}/improvements/propose")
    public ResponseEntity<List<ResumeImprovementSuggestionDto>> proposeImprovements(
            @PathVariable Long resumeId,
            @RequestParam String targetRole,
            @RequestParam(defaultValue = "ALL") String sectionFilter,
            @RequestBody List<String> missingSkills,
            Authentication authentication) {

        String userEmail = authentication.getName();
        List<ResumeImprovementSuggestionDto> list =
                resumeImprovementService.getActionableImprovements(
                        resumeId,
                        targetRole,
                        missingSkills,
                        sectionFilter,
                        userEmail
                );
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{resumeId}/improvements/apply")
    public ResponseEntity<RoleAnalysisResponseDto> applyImprovements(
            @PathVariable Long resumeId,
            @RequestParam String targetRole,
            @RequestBody List<ResumeImprovementSuggestionDto> selectedImprovements,
            Authentication authentication) throws Exception {

        String userEmail = authentication.getName();
        RoleAnalysisResponseDto updatedAnalysis =
                resumeImprovementService.applyImprovementsAndReanalyze(
                        resumeId,
                        targetRole,
                        selectedImprovements,
                        userEmail
                );

        return ResponseEntity.ok(updatedAnalysis);
    }
}