package com.pradeep.aicareerplatform.service;

import tools.jackson.databind.ObjectMapper;
import com.pradeep.aicareerplatform.config.RoleSkillConfig;
import com.pradeep.aicareerplatform.dto.ResumeExtractionDto;
import com.pradeep.aicareerplatform.dto.RoleAnalysisResponseDto;
import com.pradeep.aicareerplatform.dto.ScoreCategoryDto;
import com.pradeep.aicareerplatform.entity.Resume;
import com.pradeep.aicareerplatform.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import com.pradeep.aicareerplatform.util.SkillMatchUtil;

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
            if (SkillMatchUtil.matches(resumeSkills, skill)) {
                matched.add(skill);
            } else {
                missingRequired.add(skill);
            }
        }

        for (String skill : roleSkills.recommended()) {
            if (SkillMatchUtil.matches(resumeSkills, skill)) {
                matched.add(skill);
            } else {
                missingRecommended.add(skill);
            }
        }


        double requiredWeight = 0.7;
        double recommendedWeight = 0.3;

        int requiredTotal = roleSkills.required().size();
        int recommendedTotal = roleSkills.recommended().size();

        int requiredMatched = requiredTotal - missingRequired.size();
        int recommendedMatched = recommendedTotal - missingRecommended.size();

        double requiredScore = requiredTotal == 0 ? 1.0 : (double) requiredMatched / requiredTotal;
        double recommendedScore = recommendedTotal == 0 ? 1.0 : (double) recommendedMatched / recommendedTotal;

        int matchPercentage = (int) Math.round(
                (requiredScore * requiredWeight + recommendedScore * recommendedWeight) * 100
        );

        String suggestions = roleAnalysisAiService.generateSuggestions(
                targetRole, matched, missingRequired, missingRecommended);

        List<ScoreCategoryDto> breakdown = buildScoreBreakdown(
                extractedData, requiredScore, recommendedScore, missingRequired, missingRecommended);

        String scoreExplanation = buildScoreExplanation(missingRequired, missingRecommended);

        return new RoleAnalysisResponseDto(
                targetRole,
                matched,
                missingRequired,
                missingRecommended,
                roleSkills.required(),
                roleSkills.recommended(),
                roleSkills.optional(),
                matchPercentage,
                suggestions,
                "AI Resume Compatibility Estimate",
                breakdown,
                scoreExplanation
        );
    }

    private List<ScoreCategoryDto> buildScoreBreakdown(
            ResumeExtractionDto data,
            double requiredScore,
            double recommendedScore,
            List<String> missingRequired,
            List<String> missingRecommended) {

        List<ScoreCategoryDto> breakdown = new ArrayList<>();

        int skillsScore = (int) Math.round(requiredScore * 100);
        breakdown.add(new ScoreCategoryDto(
                "Skills Match",
                skillsScore,
                missingRequired.isEmpty()
                        ? "All required skills for this role were detected in your resume."
                        : "Missing required skills: " + String.join(", ", missingRequired) + "."
        ));

        int keywordScore = (int) Math.round(recommendedScore * 100);
        breakdown.add(new ScoreCategoryDto(
                "Keyword Match",
                keywordScore,
                missingRecommended.isEmpty()
                        ? "All commonly recommended keywords for this role were detected."
                        : "Recommended keywords not detected: " + String.join(", ", missingRecommended) + "."
        ));

        int experienceScore = data.getYearsOfExperience() > 0 ? 100 : 40;
        breakdown.add(new ScoreCategoryDto(
                "Experience",
                experienceScore,
                data.getYearsOfExperience() > 0
                        ? data.getYearsOfExperience() + " year(s) of experience detected in resume."
                        : "No clear years of experience detected — consider stating experience explicitly."
        ));

        int projectsScore = (data.getProjects() != null && !data.getProjects().isEmpty()) ? 100 : 30;
        breakdown.add(new ScoreCategoryDto(
                "Projects",
                projectsScore,
                (data.getProjects() != null && !data.getProjects().isEmpty())
                        ? data.getProjects().size() + " project(s) detected in resume."
                        : "No projects detected — adding relevant projects strengthens ATS matching."
        ));

        int educationScore = (data.getHighestEducation() != null && !data.getHighestEducation().isBlank()) ? 100 : 40;
        breakdown.add(new ScoreCategoryDto(
                "Education",
                educationScore,
                (data.getHighestEducation() != null && !data.getHighestEducation().isBlank())
                        ? "Education section detected: " + data.getHighestEducation()
                        : "No clear education section detected."
        ));


        int summaryLength = data.getSummary() != null ? data.getSummary().trim().length() : 0;
        int summaryScore = summaryLength >= 80 ? 100 : summaryLength > 0 ? 60 : 20;
        breakdown.add(new ScoreCategoryDto(
                "Summary",
                summaryScore,
                summaryLength >= 80
                        ? "Summary section is present and reasonably detailed."
                        : summaryLength > 0
                        ? "Summary is present but short — consider expanding it."
                        : "No professional summary detected."
        ));

        return breakdown;
    }

    private String buildScoreExplanation(List<String> missingRequired, List<String> missingRecommended) {
        if (missingRequired.isEmpty() && missingRecommended.isEmpty()) {
            return "Your resume covers all required and recommended skills detected for this role.";
        }
        StringBuilder sb = new StringBuilder("Your score is reduced mainly because ");
        if (!missingRequired.isEmpty()) {
            sb.append("the target role requires ")
                    .append(String.join(", ", missingRequired))
                    .append(", which ").append(missingRequired.size() == 1 ? "was" : "were")
                    .append(" not detected in your resume");
        }
        if (!missingRecommended.isEmpty()) {
            if (!missingRequired.isEmpty()) sb.append(", and ");
            sb.append("commonly recommended skills like ")
                    .append(String.join(", ", missingRecommended))
                    .append(" ").append(missingRecommended.size() == 1 ? "was" : "were")
                    .append(" also not detected");
        }
        sb.append(".");
        return sb.toString();
    }
}