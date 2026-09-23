package com.pradeep.aicareerplatform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pradeep.aicareerplatform.config.RoleSkillConfig;
import com.pradeep.aicareerplatform.dto.ResumeExtractionDto;
import com.pradeep.aicareerplatform.dto.RoleAnalysisResponseDto;
import com.pradeep.aicareerplatform.dto.ScoreCategoryDto;
import com.pradeep.aicareerplatform.entity.Resume;
import com.pradeep.aicareerplatform.repository.ResumeRepository;
import com.pradeep.aicareerplatform.util.SkillMatchUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoleAnalysisService {

    private final ResumeRepository resumeRepository;
    private final RoleSkillConfig roleSkillConfig;
    private final RoleAnalysisAiService roleAnalysisAiService;
    private final ObjectMapper objectMapper;

    private static final double WEIGHT_SKILLS_MATCH = 0.30;      // 30%
    private static final double WEIGHT_KEYWORD_MATCH = 0.25;     // 25%
    private static final double WEIGHT_EXPERIENCE = 0.20;        // 20%
    private static final double WEIGHT_PROJECTS = 0.15;          // 15%
    private static final double WEIGHT_EDUCATION = 0.05;         // 5%
    private static final double WEIGHT_SUMMARY = 0.05;           // 5%

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

        if (!resume.getUser().getEmail().equalsIgnoreCase(userEmail)) {
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

        int requiredTotal = roleSkills.required().size();
        int recommendedTotal = roleSkills.recommended().size();

        int requiredMatched = requiredTotal - missingRequired.size();
        int recommendedMatched = recommendedTotal - missingRecommended.size();

        double requiredScoreRatio = requiredTotal == 0 ? 1.0 : (double) requiredMatched / requiredTotal;
        double recommendedScoreRatio = recommendedTotal == 0 ? 1.0 : (double) recommendedMatched / recommendedTotal;

        // 1. Calculate individual category scores upfront
        int skillsScore = (int) Math.round(requiredScoreRatio * 100);
        int keywordScore = (int) Math.round(recommendedScoreRatio * 100);

        // --- FIX FOR SCORE CAP (88% -> 88%) ---
        // Dynamically compute experience instead of locking at hardcoded 40
        int experienceScore = calculateDynamicExperienceScore(extractedData, resume.getRawText());

        int projectsScore = (extractedData.getProjects() != null && !extractedData.getProjects().isEmpty()) ? 100 : 30;

        String rawEducation = extractedData.getHighestEducation();
        if (rawEducation != null) {
            rawEducation = rawEducation.replace("Artifcial", "Artificial");
        }
        int educationScore = (rawEducation != null && !rawEducation.isBlank()) ? 100 : 40;

        int summaryLength = extractedData.getSummary() != null ? extractedData.getSummary().trim().length() : 0;
        int summaryScore = summaryLength >= 80 ? 100 : summaryLength > 0 ? 60 : 20;

        // 2. Compute weighted overall match percentage
        double weightedMatch = (skillsScore * WEIGHT_SKILLS_MATCH)
                + (keywordScore * WEIGHT_KEYWORD_MATCH)
                + (experienceScore * WEIGHT_EXPERIENCE)
                + (projectsScore * WEIGHT_PROJECTS)
                + (educationScore * WEIGHT_EDUCATION)
                + (summaryScore * WEIGHT_SUMMARY);

        int overallMatchPercentage = (int) Math.round(weightedMatch);

        // 3. Build ScoreCategoryDto list directly using precomputed scores
        List<ScoreCategoryDto> breakdown = buildScoreBreakdown(
                extractedData,
                skillsScore,
                keywordScore,
                experienceScore,
                projectsScore,
                educationScore,
                summaryScore,
                rawEducation,
                missingRequired,
                missingRecommended
        );

        String suggestions = roleAnalysisAiService.generateSuggestions(
                targetRole, matched, missingRequired, missingRecommended);

        String scoreExplanation = buildScoreExplanation(missingRequired, missingRecommended, experienceScore);

        return new RoleAnalysisResponseDto(
                targetRole,
                matched,
                missingRequired,
                missingRecommended,
                roleSkills.required(),
                roleSkills.recommended(),
                roleSkills.optional(),
                overallMatchPercentage,
                suggestions,
                "AI Resume Compatibility Estimate",
                breakdown,
                scoreExplanation,
                resumeId
        );
    }

    /**
     * DYNAMIC EXPERIENCE SCORING (Fixes 0-points frozen score bug)
     */
    private int calculateDynamicExperienceScore(ResumeExtractionDto data, String rawText) {
        if (data.getYearsOfExperience() > 0) {
            return 100;
        }

        int score = 40; // Base score
        String text = (rawText != null) ? rawText.toLowerCase() : "";

        // Check for Traineeships, Internships, or Freelance projects
        if (text.contains("intern") || text.contains("trainee") || text.contains("freelance") || text.contains("developer")) {
            score += 25;
        }

        // Check for project complexity or industry technical stack presence
        if (data.getProjects() != null && !data.getProjects().isEmpty()) {
            score += 20;
        }

        // Check for quantifiable impact metrics (%, ms, APIs, database integrations)
        if (text.matches(".*\\d+%.*") || text.contains("spring boot") || text.contains("react") || text.contains("postgresql")) {
            score += 15;
        }

        return Math.min(100, score);
    }

    /**
     * PDF GENERATION HANDLER (Fixes PDF Download Failure)
     */
    public byte[] downloadResumePdf(Long resumeId) throws Exception {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        String rawText = resume.getRawText() != null ? resume.getRawText() : "Resume Content";

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(40, 750);

                // Write formatted lines into PDF
                String[] lines = rawText.split("\n");
                int lineCount = 0;
                for (String line : lines) {
                    if (lineCount > 40) break; // Keep within single page boundaries for preview download
                    contentStream.showText(line.replaceAll("[^\\x00-\\x7F]", "")); // Strip unprintable chars
                    contentStream.newLineAtOffset(0, -15);
                    lineCount++;
                }
                contentStream.endText();
            }

            document.save(baos);
            return baos.toByteArray();
        }
    }

    private List<ScoreCategoryDto> buildScoreBreakdown(
            ResumeExtractionDto data,
            int skillsScore,
            int keywordScore,
            int experienceScore,
            int projectsScore,
            int educationScore,
            int summaryScore,
            String sanitizedEducation,
            List<String> missingRequired,
            List<String> missingRecommended) {

        List<ScoreCategoryDto> breakdown = new ArrayList<>();

        breakdown.add(new ScoreCategoryDto(
                "Skills Match",
                skillsScore,
                missingRequired.isEmpty()
                        ? "All required skills for this role were detected in your resume."
                        : "Missing required skills: " + String.join(", ", missingRequired) + "."
        ));

        breakdown.add(new ScoreCategoryDto(
                "Keyword Match",
                keywordScore,
                missingRecommended.isEmpty()
                        ? "All commonly recommended keywords for this role were detected."
                        : "Recommended keywords not detected: " + String.join(", ", missingRecommended) + "."
        ));

        breakdown.add(new ScoreCategoryDto(
                "Experience",
                experienceScore,
                data.getYearsOfExperience() > 0
                        ? data.getYearsOfExperience() + " year(s) of experience detected in resume."
                        : "Hands-on project and training experience evaluated."
        ));

        breakdown.add(new ScoreCategoryDto(
                "Projects",
                projectsScore,
                (data.getProjects() != null && !data.getProjects().isEmpty())
                        ? data.getProjects().size() + " project(s) detected in resume."
                        : "No projects detected — adding relevant projects strengthens ATS matching."
        ));

        breakdown.add(new ScoreCategoryDto(
                "Education",
                educationScore,
                (sanitizedEducation != null && !sanitizedEducation.isBlank())
                        ? "Education section detected: " + sanitizedEducation
                        : "No clear education section detected."
        ));

        breakdown.add(new ScoreCategoryDto(
                "Summary",
                summaryScore,
                data.getSummary() != null && data.getSummary().trim().length() >= 80
                        ? "Summary section is present and reasonably detailed."
                        : data.getSummary() != null && !data.getSummary().trim().isEmpty()
                        ? "Summary is present but short — consider expanding it."
                        : "No professional summary detected."
        ));

        return breakdown;
    }

    private String buildScoreExplanation(List<String> missingRequired, List<String> missingRecommended, int experienceScore) {
        if (missingRequired.isEmpty() && missingRecommended.isEmpty() && experienceScore == 100) {
            return "Your resume covers all required skills, keywords, and experience metrics for this role.";
        }
        StringBuilder sb = new StringBuilder("Your compatibility score is adjusted based on: ");
        if (!missingRequired.isEmpty()) {
            sb.append("missing core skills (").append(String.join(", ", missingRequired)).append(")");
        }
        if (!missingRecommended.isEmpty()) {
            if (!missingRequired.isEmpty()) sb.append(", ");
            sb.append("missing keywords (").append(String.join(", ", missingRecommended)).append(")");
        }
        if (experienceScore < 100) {
            if (!missingRequired.isEmpty() || !missingRecommended.isEmpty()) sb.append(", and ");
            sb.append("unclear hands-on work experience duration");
        }
        sb.append(".");
        return sb.toString();
    }
}