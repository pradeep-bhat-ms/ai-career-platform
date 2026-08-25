package com.pradeep.aicareerplatform.service;

import tools.jackson.databind.ObjectMapper;
import com.pradeep.aicareerplatform.config.RoleSkillConfig;
import com.pradeep.aicareerplatform.dto.ResumeExtractionDto;
import com.pradeep.aicareerplatform.dto.RoleAnalysisResponseDto;
import com.pradeep.aicareerplatform.entity.Resume;
import com.pradeep.aicareerplatform.repository.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleAnalysisService {

    private final ResumeRepository resumeRepository;
    private final RoleSkillConfig roleSkillConfig;
    private final RoleAnalysisAiService roleAnalysisAiService;
    private final ObjectMapper objectMapper;

    public RoleAnalysisService(ResumeRepository resumeRepository,
                               RoleSkillConfig roleSkillConfig,
                               RoleAnalysisAiService roleAnalysisAiService,
                               ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.roleSkillConfig = roleSkillConfig;
        this.roleAnalysisAiService = roleAnalysisAiService;
        this.objectMapper = objectMapper;
    }



    public RoleAnalysisResponseDto analyzeForRole(Long resumeId, String targetRole, String userEmail) throws Exception {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        if (!resume.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You do not have access to this resume");
        }

        if (resume.getExtractedDataJson() == null) {
            throw new IllegalStateException("Resume must be analyzed first before target role analysis");
        }

        ResumeExtractionDto extractedData = objectMapper.readValue(
                resume.getExtractedDataJson(), ResumeExtractionDto.class);

        List<String> resumeSkills = extractedData.getTechnicalSkills();

        RoleSkillConfig.RoleSkills roleSkills = roleSkillConfig.getSkillsForRole(targetRole);

        List<String> matched = new ArrayList<>();
        List<String> missingRequired = new ArrayList<>();
        List<String> missingRecommended = new ArrayList<>();

        for (String skill : roleSkills.required()) {
            if (containsIgnoreCase(resumeSkills, skill)) {
                matched.add(skill);
            } else {
                missingRequired.add(skill);
            }
        }

        for (String skill : roleSkills.recommended()) {
            if (containsIgnoreCase(resumeSkills, skill)) {
                matched.add(skill);
            } else {
                missingRecommended.add(skill);
            }
        }

        int totalConsidered = roleSkills.required().size() + roleSkills.recommended().size();
        int matchPercentage = totalConsidered == 0 ? 0 : (matched.size() * 100) / totalConsidered;

        String suggestions = roleAnalysisAiService.generateSuggestions(
                targetRole, matched, missingRequired, missingRecommended);

        return new RoleAnalysisResponseDto(
                targetRole,
                matched,
                missingRequired,
                missingRecommended,
                roleSkills.required(),
                roleSkills.recommended(),
                roleSkills.optional(),
                matchPercentage,
                suggestions
        );
    }


    private boolean containsIgnoreCase(List<String> resumeSkills, String requiredSkill) {
        String normalizedRequired = normalize(requiredSkill);
        return resumeSkills.stream().anyMatch(resumeSkill -> {
            String normalizedResume = normalize(resumeSkill);
            return normalizedResume.contains(normalizedRequired) || normalizedRequired.contains(normalizedResume);
        });
    }

    private String normalize(String skill) {
        return skill.toLowerCase()
                .replaceAll("s$", "")
                .replaceAll("[.\\-_]", "");
    }
}