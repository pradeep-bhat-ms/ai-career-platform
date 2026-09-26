package com.pradeep.aicareerplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.*;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;



@Service
public class RoleAnalysisService {

    private final ResumeRepository resumeRepository;
    private final RoleSkillConfig roleSkillConfig;
    private final RoleAnalysisAiService roleAnalysisAiService;
    private final ObjectMapper objectMapper;

    private static final double WEIGHT_SKILLS = 0.30;
    private static final double WEIGHT_KEYWORDS = 0.20;
    private static final double WEIGHT_EXPERIENCE = 0.20;
    private static final double WEIGHT_PROJECTS = 0.20;
    private static final double WEIGHT_EDUCATION = 0.05;
    private static final double WEIGHT_SUMMARY = 0.05;

    public RoleAnalysisService(
            ResumeRepository resumeRepository,
            RoleSkillConfig roleSkillConfig,
            RoleAnalysisAiService roleAnalysisAiService,
            ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.roleSkillConfig = roleSkillConfig;
        this.roleAnalysisAiService = roleAnalysisAiService;
        this.objectMapper = objectMapper;
    }

    public RoleAnalysisResponseDto analyzeForRole(
            Long resumeId,
            String targetRole,
            String userEmail) throws Exception {

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Resume not found"));

        if (resume.getUser() == null ||
                resume.getUser().getEmail() == null ||
                !resume.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new IllegalArgumentException(
                    "You do not have access to this resume");
        }

        if (resume.getExtractedDataJson() == null ||
                resume.getExtractedDataJson().isBlank()) {
            throw new IllegalStateException(
                    "Resume must be analyzed first before target role analysis");
        }

        if (targetRole == null || targetRole.isBlank()) {
            throw new IllegalArgumentException("Target role is required");
        }

        ResumeExtractionDto extractedData =
                objectMapper.readValue(
                        resume.getExtractedDataJson(),
                        ResumeExtractionDto.class
                );

        List<String> resumeSkills =
                extractedData.getTechnicalSkills() != null
                        ? extractedData.getTechnicalSkills()
                        : Collections.emptyList();

        RoleSkillConfig.RoleSkills roleSkills =
                roleSkillConfig.getSkillsForRole(targetRole);

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingRequired = new ArrayList<>();
        List<String> missingRecommended = new ArrayList<>();

        for (String skill : safeList(roleSkills.required())) {
            if (SkillMatchUtil.matches(resumeSkills, skill)) {
                addIfMissing(matchedSkills, skill);
            } else {
                missingRequired.add(skill);
            }
        }

        for (String keyword : safeList(roleSkills.recommended())) {
            if (SkillMatchUtil.matches(resumeSkills, keyword)) {
                addIfMissing(matchedSkills, keyword);
            } else {
                missingRecommended.add(keyword);
            }
        }

        int requiredTotal = countValid(roleSkills.required());
        int requiredMatched =
                requiredTotal - missingRequired.size();

        int skillsScore =
                requiredTotal == 0
                        ? 100
                        : clampScore(
                        (int) Math.round(
                                ((double) requiredMatched /
                                        requiredTotal) * 100
                        )
                );

        int keywordScore =
                calculateProgressiveKeywordScore(
                        resumeSkills,
                        roleSkills,
                        targetRole
                );

        int experienceScore =
                calculateExperienceScore(
                        extractedData,
                        resume.getRawText(),
                        roleSkills
                );

        int projectsScore =
                calculateProjectScore(
                        extractedData,
                        roleSkills,
                        targetRole
                );

        String education =
                sanitizeEducation(
                        extractedData.getHighestEducation()
                );

        int educationScore =
                calculateEducationScore(
                        education,
                        targetRole,
                        roleSkills
                );

        int summaryScore =
                calculateSummaryScore(
                        extractedData.getSummary(),
                        targetRole,
                        roleSkills
                );

        double weightedScore =
                (skillsScore * WEIGHT_SKILLS)
                        + (keywordScore * WEIGHT_KEYWORDS)
                        + (experienceScore * WEIGHT_EXPERIENCE)
                        + (projectsScore * WEIGHT_PROJECTS)
                        + (educationScore * WEIGHT_EDUCATION)
                        + (summaryScore * WEIGHT_SUMMARY);

        int overallScore =
                clampScore(
                        (int) Math.round(weightedScore)
                );

        List<ScoreCategoryDto> breakdown =
                buildScoreBreakdown(
                        extractedData,
                        targetRole,
                        skillsScore,
                        keywordScore,
                        experienceScore,
                        projectsScore,
                        educationScore,
                        summaryScore,
                        education,
                        missingRequired,
                        missingRecommended
                );

        String suggestions =
                roleAnalysisAiService.generateSuggestions(
                        targetRole,
                        matchedSkills,
                        missingRequired,
                        missingRecommended
                );

        String scoreExplanation =
                buildScoreExplanation(
                        missingRequired,
                        missingRecommended,
                        experienceScore,
                        projectsScore,
                        educationScore,
                        summaryScore
                );

        return new RoleAnalysisResponseDto(
                targetRole,
                matchedSkills,
                missingRequired,
                missingRecommended,
                roleSkills.required(),
                roleSkills.recommended(),
                roleSkills.optional(),
                overallScore,
                suggestions,
                "AI Resume Compatibility Estimate",
                breakdown,
                scoreExplanation,
                resumeId
        );
    }

    private int calculateProgressiveKeywordScore(
            List<String> resumeSkills,
            RoleSkillConfig.RoleSkills roleSkills,
            String targetRole) {

        List<String> allKeywords = new ArrayList<>();

        allKeywords.addAll(safeList(roleSkills.required()));
        allKeywords.addAll(safeList(roleSkills.recommended()));

        if (allKeywords.isEmpty()) {
            return 100;
        }

        List<String> basic = new ArrayList<>();
        List<String> intermediate = new ArrayList<>();
        List<String> advanced = new ArrayList<>();

        for (String keyword : allKeywords) {

            if (keyword == null || keyword.isBlank()) {
                continue;
            }

            if (isAdvancedKeyword(keyword, targetRole)) {
                addIfMissing(advanced, keyword);
            } else if (isIntermediateKeyword(keyword, targetRole)) {
                addIfMissing(intermediate, keyword);
            } else {
                addIfMissing(basic, keyword);
            }
        }

        double weighted = 0;
        double totalWeight = 0;

        if (!basic.isEmpty()) {
            double score =
                    calculateKeywordLevelScore(
                            resumeSkills,
                            basic
                    );

            weighted += score * 0.50;
            totalWeight += 0.50;
        }

        if (!intermediate.isEmpty()) {
            double score =
                    calculateKeywordLevelScore(
                            resumeSkills,
                            intermediate
                    );

            weighted += score * 0.30;
            totalWeight += 0.30;
        }

        if (!advanced.isEmpty()) {
            double score =
                    calculateKeywordLevelScore(
                            resumeSkills,
                            advanced
                    );

            weighted += score * 0.20;
            totalWeight += 0.20;
        }

        if (totalWeight == 0) {
            return 100;
        }

        return clampScore(
                (int) Math.round(
                        weighted / totalWeight
                )
        );
    }

    private boolean isAdvancedKeyword(
            String keyword,
            String targetRole) {

        String value = normalize(keyword);

        return containsAny(
                value,
                "microservices",
                "microservice",
                "docker",
                "kubernetes",
                "aws",
                "azure",
                "gcp",
                "kafka",
                "redis",
                "rabbitmq",
                "terraform",
                "jenkins",
                "ci/cd",
                "cicd",
                "system design",
                "distributed systems",
                "distributed system",
                "cloud architecture"
        );
    }

    private boolean isIntermediateKeyword(
            String keyword,
            String targetRole) {

        String value = normalize(keyword);

        return containsAny(
                value,
                "spring",
                "spring boot",
                "spring security",
                "hibernate",
                "jpa",
                "rest api",
                "rest",
                "jwt",
                "authentication",
                "authorization",
                "mysql",
                "postgresql",
                "mongodb",
                "react",
                "react.js",
                "redux",
                "typescript",
                "junit",
                "mockito",
                "testing",
                "maven",
                "git",
                "github",
                "bootstrap",
                "thymeleaf"
        );
    }

    private double calculateKeywordLevelScore(
            List<String> resumeSkills,
            List<String> keywords) {

        if (keywords == null || keywords.isEmpty()) {
            return 100;
        }

        int matched = 0;
        int total = 0;

        for (String keyword : keywords) {

            if (keyword == null || keyword.isBlank()) {
                continue;
            }

            total++;

            if (SkillMatchUtil.matches(
                    resumeSkills,
                    keyword)) {
                matched++;
            }
        }

        if (total == 0) {
            return 100;
        }

        return ((double) matched / total) * 100;
    }

    private int calculateProjectScore(
            ResumeExtractionDto data,
            RoleSkillConfig.RoleSkills roleSkills,
            String targetRole) {

        if (data.getProjects() == null ||
                data.getProjects().isEmpty()) {
            return 0;
        }

        List<Double> projectScores = new ArrayList<>();

        Set<String> roleKeywords =
                buildRoleKeywords(
                        roleSkills,
                        targetRole
                );

        for (Object project : data.getProjects()) {

            String projectText =
                    normalize(
                            convertProjectToText(project)
                    );

            if (projectText.isBlank()) {
                continue;
            }

            double requiredScore =
                    calculateProjectSkillScore(
                            projectText,
                            roleSkills.required()
                    );

            double recommendedScore =
                    calculateProjectSkillScore(
                            projectText,
                            roleSkills.recommended()
                    );

            double optionalScore =
                    calculateProjectSkillScore(
                            projectText,
                            roleSkills.optional()
                    );

            double roleScore =
                    calculateTermCoverage(
                            projectText,
                            roleKeywords
                    );

            double technicalDepth =
                    calculateTechnicalDepth(
                            projectText,
                            roleSkills
                    );

            double evidenceScore =
                    calculateProjectEvidence(
                            projectText
                    );

            double descriptionScore =
                    calculateProjectDescriptionScore(
                            projectText
                    );

            boolean hasRelevantEvidence =
                    requiredScore > 0 ||
                            recommendedScore > 0 ||
                            roleScore > 0;

            if (!hasRelevantEvidence) {
                projectScores.add(
                        Math.min(
                                25,
                                technicalDepth * 0.40
                                        + evidenceScore * 0.30
                                        + descriptionScore * 0.30
                        )
                );

                continue;
            }

            double projectScore =
                    (requiredScore * 0.40)
                            + (recommendedScore * 0.15)
                            + (optionalScore * 0.05)
                            + (roleScore * 0.20)
                            + (technicalDepth * 0.10)
                            + (evidenceScore * 0.07)
                            + (descriptionScore * 0.03);

            projectScores.add(
                    Math.min(
                            100,
                            projectScore
                    )
            );
        }

        if (projectScores.isEmpty()) {
            return 0;
        }

        projectScores.sort(
                Comparator.reverseOrder()
        );

        int numberToUse =
                Math.min(
                        2,
                        projectScores.size()
                );

        double total = 0;

        for (int i = 0; i < numberToUse; i++) {
            total += projectScores.get(i);
        }

        return clampScore(
                (int) Math.round(
                        total / numberToUse
                )
        );
    }

    private double calculateProjectSkillScore(
            String projectText,
            List<String> skills) {

        if (skills == null || skills.isEmpty()) {
            return 0;
        }

        int matched = 0;
        int total = 0;

        for (String skill : skills) {

            if (skill == null || skill.isBlank()) {
                continue;
            }

            total++;

            if (containsTerm(
                    projectText,
                    skill)) {
                matched++;
            }
        }

        if (total == 0) {
            return 0;
        }

        return ((double) matched / total) * 100;
    }

    private double calculateTermCoverage(
            String projectText,
            Set<String> keywords) {

        if (keywords == null || keywords.isEmpty()) {
            return 0;
        }

        int matched = 0;
        int total = 0;

        for (String keyword : keywords) {

            if (keyword == null || keyword.isBlank()) {
                continue;
            }

            total++;

            if (containsTerm(
                    projectText,
                    keyword)) {
                matched++;
            }
        }

        if (total == 0) {
            return 0;
        }

        return ((double) matched / total) * 100;
    }

    private double calculateTechnicalDepth(
            String projectText,
            RoleSkillConfig.RoleSkills roleSkills) {

        Set<String> indicators = new HashSet<>();

        indicators.addAll(
                safeList(roleSkills.required())
        );

        indicators.addAll(
                safeList(roleSkills.recommended())
        );

        indicators.addAll(
                Arrays.asList(
                        "java",
                        "python",
                        "javascript",
                        "html",
                        "css",
                        "react",
                        "spring",
                        "spring boot",
                        "spring security",
                        "hibernate",
                        "jpa",
                        "rest",
                        "rest api",
                        "jwt",
                        "authentication",
                        "authorization",
                        "mysql",
                        "postgresql",
                        "mongodb",
                        "sql",
                        "crud",
                        "testing",
                        "junit",
                        "maven",
                        "git",
                        "github",
                        "docker",
                        "aws",
                        "payment",
                        "razorpay"
                )
        );

        int matched = 0;

        for (String indicator : indicators) {
            if (containsTerm(
                    projectText,
                    indicator)) {
                matched++;
            }
        }

        if (matched == 0) {
            return 0;
        }

        return Math.min(
                100,
                matched * 10.0
        );
    }

    private double calculateProjectEvidence(
            String projectText) {

        double score = 0;

        if (containsAny(
                projectText,
                "github",
                "repository",
                "source code",
                "git"
        )) {
            score += 40;
        }

        if (containsAny(
                projectText,
                "live demo",
                "deployed",
                "deployment",
                "render",
                "vercel",
                "netlify"
        )) {
            score += 35;
        }

        if (containsAny(
                projectText,
                "implemented",
                "developed",
                "built",
                "designed",
                "integrated",
                "created"
        )) {
            score += 25;
        }

        return Math.min(
                100,
                score
        );
    }

    private double calculateProjectDescriptionScore(
            String projectText) {

        int length = projectText.length();

        if (length >= 250) {
            return 100;
        }

        if (length >= 150) {
            return 80;
        }

        if (length >= 80) {
            return 60;
        }

        if (length >= 30) {
            return 40;
        }

        return 20;
    }

    private int calculateExperienceScore(
            ResumeExtractionDto data,
            String rawText,
            RoleSkillConfig.RoleSkills roleSkills) {

        String text =
                normalize(
                        rawText != null
                                ? rawText
                                : ""
                );

        double years =
                Math.max(
                        0,
                        data.getYearsOfExperience()
                );

        boolean hasProfessionalExperience =
                containsAny(
                        text,
                        "work experience",
                        "professional experience",
                        "employment",
                        "software engineer",
                        "software developer",
                        "java developer",
                        "backend developer",
                        "frontend developer",
                        "full stack developer",
                        "full-stack developer"
                );

        boolean hasInternship =
                containsAny(
                        text,
                        "internship",
                        "intern",
                        "trainee"
                );

        boolean hasTraining =
                containsAny(
                        text,
                        "training",
                        "bootcamp",
                        "workshop"
                );

        if (years <= 0 &&
                !hasProfessionalExperience &&
                !hasInternship) {

            return hasTraining
                    ? 35
                    : 20;
        }

        List<String> targetSkills =
                new ArrayList<>();

        targetSkills.addAll(
                safeList(roleSkills.required())
        );

        targetSkills.addAll(
                safeList(roleSkills.recommended())
        );

        int matched = 0;
        int total = 0;

        for (String skill : targetSkills) {

            if (skill == null || skill.isBlank()) {
                continue;
            }

            total++;

            if (containsTerm(
                    text,
                    skill)) {
                matched++;
            }
        }

        double relevance =
                total == 0
                        ? 0
                        : ((double) matched / total) * 100;

        double baseScore;

        if (years >= 3) {
            baseScore = 80;
        } else if (years >= 2) {
            baseScore = 70;
        } else if (years >= 1) {
            baseScore = 60;
        } else if (hasProfessionalExperience) {
            baseScore = 55;
        } else if (hasInternship) {
            baseScore = 50;
        } else {
            baseScore = 35;
        }

        double score =
                (baseScore * 0.65)
                        + (relevance * 0.35);

        if (hasInternship && years <= 0) {
            score = Math.min(
                    score,
                    70
            );
        }

        return clampScore(
                (int) Math.round(score)
        );
    }

    private int calculateEducationScore(
            String education,
            String targetRole,
            RoleSkillConfig.RoleSkills roleSkills) {

        if (education == null ||
                education.isBlank()) {
            return 20;
        }

        String text =
                normalize(education);

        double score = 60;

        if (containsAny(
                text,
                "computer science",
                "computer applications",
                "information technology",
                "software engineering",
                "data science",
                "artificial intelligence",
                "machine learning"
        )) {
            score += 25;
        }

        for (String skill :
                safeList(roleSkills.required())) {

            if (containsTerm(
                    text,
                    skill)) {
                score += 15;
                break;
            }
        }

        return clampScore(
                (int) Math.round(
                        Math.min(
                                100,
                                score
                        )
                )
        );
    }

    private int calculateSummaryScore(
            String summary,
            String targetRole,
            RoleSkillConfig.RoleSkills roleSkills) {

        if (summary == null ||
                summary.isBlank()) {
            return 20;
        }

        String text =
                normalize(summary);

        double score = 0;

        if (text.length() >= 250) {
            score += 35;
        } else if (text.length() >= 150) {
            score += 30;
        } else if (text.length() >= 80) {
            score += 22;
        } else if (text.length() >= 40) {
            score += 15;
        } else {
            score += 8;
        }

        if (containsTerm(
                text,
                targetRole)) {
            score += 25;
        }

        List<String> required =
                safeList(roleSkills.required());

        if (!required.isEmpty()) {

            int matched = 0;
            int total = 0;

            for (String skill : required) {

                if (skill == null ||
                        skill.isBlank()) {
                    continue;
                }

                total++;

                if (containsTerm(
                        text,
                        skill)) {
                    matched++;
                }
            }

            if (total > 0) {
                score +=
                        (((double) matched / total) * 100)
                                * 0.25;
            }
        }

        if (containsAny(
                text,
                "develop",
                "developing",
                "build",
                "building",
                "design",
                "designed",
                "implement",
                "implemented",
                "engineer",
                "engineering",
                "specialize",
                "specialized"
        )) {
            score += 15;
        }

        return clampScore(
                (int) Math.round(
                        Math.min(
                                100,
                                score
                        )
                )
        );
    }

    private List<ScoreCategoryDto> buildScoreBreakdown(
            ResumeExtractionDto data,
            String targetRole,
            int skillsScore,
            int keywordScore,
            int experienceScore,
            int projectsScore,
            int educationScore,
            int summaryScore,
            String education,
            List<String> missingRequired,
            List<String> missingRecommended) {

        List<ScoreCategoryDto> breakdown =
                new ArrayList<>();

        String skillsDescription;

        if (missingRequired.isEmpty()) {
            skillsDescription =
                    "All configured required skills for this role were detected in your resume.";
        } else {
            skillsDescription =
                    "Skills not detected in your resume for this target role: "
                            + String.join(
                            ", ",
                            missingRequired
                    )
                            + ".";
        }

        breakdown.add(
                new ScoreCategoryDto(
                        "Skills Match",
                        skillsScore,
                        skillsDescription
                )
        );

        String keywordDescription;

        if (missingRecommended.isEmpty()) {
            keywordDescription =
                    "Basic, intermediate, and advanced role keywords detected.";
        } else {
            keywordDescription =
                    "Keywords are evaluated progressively from basic to intermediate to advanced.";
        }

        breakdown.add(
                new ScoreCategoryDto(
                        "Keyword Match",
                        keywordScore,
                        keywordDescription
                )
        );

        String experienceDescription;

        if (experienceScore >= 80) {
            experienceDescription =
                    "Strong evidence of role-relevant professional experience was detected.";
        } else if (experienceScore >= 60) {
            experienceDescription =
                    "Relevant professional or practical experience was detected.";
        } else if (experienceScore >= 40) {
            experienceDescription =
                    "Relevant internship or practical experience was detected.";
        } else if (experienceScore >= 30) {
            experienceDescription =
                    "Training or early practical experience signals were detected.";
        } else {
            experienceDescription =
                    "No clear role-relevant experience evidence was detected.";
        }

        breakdown.add(
                new ScoreCategoryDto(
                        "Experience",
                        experienceScore,
                        experienceDescription
                )
        );

        int projectCount =
                data.getProjects() != null
                        ? data.getProjects().size()
                        : 0;

        String projectDescription;

        if (projectCount == 0) {
            projectDescription =
                    "No projects were detected in the resume.";
        } else if (projectsScore >= 80) {
            projectDescription =
                    "The strongest two relevant projects show strong alignment with "
                            + targetRole
                            + ".";
        } else if (projectsScore >= 60) {
            projectDescription =
                    "The strongest two relevant projects show good alignment with "
                            + targetRole
                            + ".";
        } else if (projectsScore >= 40) {
            projectDescription =
                    "Relevant project skills and technical evidence were detected.";
        } else {
            projectDescription =
                    "Projects were evaluated for target-role skills, keywords, and technical relevance.";
        }

        breakdown.add(
                new ScoreCategoryDto(
                        "Projects",
                        projectsScore,
                        projectDescription
                )
        );

        String educationDescription;

        if (education != null &&
                !education.isBlank()) {
            educationDescription =
                    "Education detected: "
                            + education;
        } else {
            educationDescription =
                    "No clear education information was detected.";
        }

        breakdown.add(
                new ScoreCategoryDto(
                        "Education",
                        educationScore,
                        educationDescription
                )
        );

        String summaryDescription;

        if (summaryScore >= 80) {
            summaryDescription =
                    "Summary contains useful role and skill information.";
        } else if (summaryScore >= 50) {
            summaryDescription =
                    "Summary is present but could better align with the target role.";
        } else {
            summaryDescription =
                    "Summary is missing or provides limited role-specific information.";
        }

        breakdown.add(
                new ScoreCategoryDto(
                        "Professional Summary",
                        summaryScore,
                        summaryDescription
                )
        );

        return breakdown;
    }

    private String buildScoreExplanation(
            List<String> missingRequired,
            List<String> missingRecommended,
            int experienceScore,
            int projectsScore,
            int educationScore,
            int summaryScore) {

        List<String> factors =
                new ArrayList<>();

        if (!missingRequired.isEmpty()) {
            factors.add(
                    "skills not detected: "
                            + String.join(
                            ", ",
                            missingRequired
                    )
            );
        }

        if (!missingRecommended.isEmpty()) {
            factors.add(
                    "some role keywords were not detected"
            );
        }

        if (experienceScore < 60) {
            factors.add(
                    "limited role-relevant professional experience evidence"
            );
        }

        if (projectsScore < 60) {
            factors.add(
                    "project relevance or technical evidence could be stronger"
            );
        }

        if (educationScore < 60) {
            factors.add(
                    "education information has limited detected relevance"
            );
        }

        if (summaryScore < 60) {
            factors.add(
                    "summary could be more aligned with the target role"
            );
        }

        if (factors.isEmpty()) {
            return "The compatibility score is based on required skills, progressive role keywords, experience, the strongest two relevant projects, education, and professional summary.";
        }

        return "The compatibility score is influenced by: "
                + String.join(
                "; ",
                factors
        )
                + ".";
    }

    private Set<String> buildRoleKeywords(
            RoleSkillConfig.RoleSkills roleSkills,
            String targetRole) {

        Set<String> keywords =
                new HashSet<>();

        keywords.addAll(
                safeList(roleSkills.required())
        );

        keywords.addAll(
                safeList(roleSkills.recommended())
        );

        String role =
                targetRole != null
                        ? targetRole.toLowerCase()
                        : "";

        if (role.contains("java")) {
            keywords.addAll(
                    Arrays.asList(
                            "java",
                            "oop",
                            "spring",
                            "spring boot",
                            "spring security",
                            "hibernate",
                            "jpa",
                            "rest",
                            "rest api",
                            "sql",
                            "mysql",
                            "postgresql",
                            "jwt",
                            "maven",
                            "git"
                    )
            );
        }

        if (role.contains("full stack") ||
                role.contains("full-stack")) {

            keywords.addAll(
                    Arrays.asList(
                            "frontend",
                            "backend",
                            "java",
                            "javascript",
                            "react",
                            "html",
                            "html5",
                            "css",
                            "css3",
                            "spring",
                            "spring boot",
                            "spring security",
                            "hibernate",
                            "jpa",
                            "rest",
                            "rest api",
                            "sql",
                            "mysql",
                            "postgresql",
                            "jwt",
                            "maven",
                            "git",
                            "github"
                    )
            );
        }

        if (role.contains("software engineer") ||
                role.contains("software developer")) {

            keywords.addAll(
                    Arrays.asList(
                            "java",
                            "python",
                            "javascript",
                            "oop",
                            "data structures",
                            "algorithms",
                            "spring boot",
                            "rest api",
                            "sql",
                            "git",
                            "github",
                            "testing"
                    )
            );
        }

        if (role.contains("frontend") ||
                role.contains("front end")) {

            keywords.addAll(
                    Arrays.asList(
                            "html",
                            "html5",
                            "css",
                            "css3",
                            "javascript",
                            "react",
                            "typescript",
                            "redux",
                            "responsive design"
                    )
            );
        }

        if (role.contains("backend") ||
                role.contains("back end")) {

            keywords.addAll(
                    Arrays.asList(
                            "java",
                            "spring",
                            "spring boot",
                            "spring security",
                            "hibernate",
                            "jpa",
                            "rest",
                            "rest api",
                            "sql",
                            "database",
                            "jwt",
                            "authentication",
                            "authorization"
                    )
            );
        }

        if (role.contains("devops")) {

            keywords.addAll(
                    Arrays.asList(
                            "docker",
                            "kubernetes",
                            "ci/cd",
                            "jenkins",
                            "aws",
                            "linux",
                            "terraform",
                            "deployment"
                    )
            );
        }

        if (role.contains("data analyst")) {

            keywords.addAll(
                    Arrays.asList(
                            "sql",
                            "excel",
                            "python",
                            "pandas",
                            "power bi",
                            "tableau",
                            "data analysis",
                            "data visualization"
                    )
            );
        }

        if (role.contains("data engineer")) {

            keywords.addAll(
                    Arrays.asList(
                            "python",
                            "sql",
                            "etl",
                            "data pipeline",
                            "spark",
                            "hadoop",
                            "kafka",
                            "airflow"
                    )
            );
        }

        return keywords;
    }

    private String convertProjectToText(
            Object project) {

        if (project == null) {
            return "";
        }

        try {
            JsonNode node =
                    objectMapper.valueToTree(
                            project
                    );

            return node.toString();

        } catch (Exception e) {
            return String.valueOf(
                    project
            );
        }
    }

    private String normalize(
            String text) {

        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9+#./\\-\\s]",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private boolean containsTerm(
            String text,
            String term) {

        if (text == null ||
                text.isBlank() ||
                term == null ||
                term.isBlank()) {
            return false;
        }

        String normalizedText =
                normalize(text);

        String normalizedTerm =
                normalize(term);

        if (normalizedTerm.isBlank()) {
            return false;
        }

        String paddedText =
                " "
                        + normalizedText
                        + " ";

        String paddedTerm =
                " "
                        + normalizedTerm
                        + " ";

        if (paddedText.contains(
                paddedTerm)) {
            return true;
        }

        if (normalizedTerm.equals("react.js") &&
                paddedText.contains(
                        " react ")) {
            return true;
        }

        if (normalizedTerm.equals("react") &&
                paddedText.contains(
                        " react.js ")) {
            return true;
        }

        if (normalizedTerm.equals("html5") &&
                paddedText.contains(
                        " html ")) {
            return true;
        }

        if (normalizedTerm.equals("html") &&
                paddedText.contains(
                        " html5 ")) {
            return true;
        }

        if (normalizedTerm.equals("css3") &&
                paddedText.contains(
                        " css ")) {
            return true;
        }

        if (normalizedTerm.equals("css") &&
                paddedText.contains(
                        " css3 ")) {
            return true;
        }

        if (normalizedTerm.equals("full-stack") &&
                paddedText.contains(
                        " full stack ")) {
            return true;
        }

        if (normalizedTerm.equals("full stack") &&
                paddedText.contains(
                        " full-stack ")) {
            return true;
        }

        if (normalizedTerm.equals("ci/cd")) {
            return paddedText.contains(" ci cd ")
                    || paddedText.contains(" cicd ");
        }

        if (normalizedTerm.equals("cicd")) {
            return paddedText.contains(" ci cd ")
                    || paddedText.contains(" ci/cd ");
        }

        if (normalizedTerm.equals("rest api") &&
                paddedText.contains(
                        " rest ")) {
            return true;
        }

        return false;
    }

    private boolean containsAny(
            String text,
            String... terms) {

        if (text == null) {
            return false;
        }

        for (String term : terms) {
            if (containsTerm(
                    text,
                    term)) {
                return true;
            }
        }

        return false;
    }

    private void addIfMissing(
            List<String> list,
            String value) {

        if (!containsIgnoreCase(
                list,
                value)) {
            list.add(value);
        }
    }

    private boolean containsIgnoreCase(
            List<String> list,
            String value) {

        if (list == null ||
                value == null) {
            return false;
        }

        for (String item : list) {

            if (item != null &&
                    item.equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }

    private List<String> safeList(
            List<String> values) {

        return values != null
                ? values
                : Collections.emptyList();
    }

    private int countValid(
            List<String> values) {

        if (values == null) {
            return 0;
        }

        int count = 0;

        for (String value : values) {

            if (value != null &&
                    !value.isBlank()) {
                count++;
            }
        }

        return count;
    }

    private String sanitizeEducation(
            String education) {

        if (education == null) {
            return null;
        }

        return education
                .replace(
                        "Artifcial",
                        "Artificial"
                )
                .trim();
    }

    private int clampScore(
            int score) {

        return Math.max(
                0,
                Math.min(
                        100,
                        score
                )
        );
    }

    public byte[] downloadResumePdf(
            Long resumeId) throws Exception {

        Resume resume =
                resumeRepository.findById(
                        resumeId
                ).orElseThrow(
                        () -> new IllegalArgumentException(
                                "Resume not found"
                        )
                );

        String rawText =
                resume.getRawText() != null
                        ? resume.getRawText()
                        : "Resume Content";

        try (
                PDDocument document =
                        new PDDocument();

                ByteArrayOutputStream baos =
                        new ByteArrayOutputStream()
        ) {

            PDPage page =
                    new PDPage();

            document.addPage(page);

            PDPageContentStream stream =
                    new PDPageContentStream(
                            document,
                            page
                    );

            stream.setFont(
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                    10
            );

            stream.beginText();
            stream.setLeading(14);
            stream.newLineAtOffset(
                    40,
                    750
            );

            int lineCount = 0;

            for (String line :
                    rawText.split("\\r?\\n")) {

                String safeLine =
                        line.replaceAll(
                                "[^\\x00-\\x7F]",
                                ""
                        );

                if (safeLine.length() > 110) {
                    safeLine =
                            safeLine.substring(
                                    0,
                                    110
                            );
                }

                if (lineCount >= 48) {
                    stream.endText();
                    stream.close();

                    page =
                            new PDPage();

                    document.addPage(page);

                    stream =
                            new PDPageContentStream(
                                    document,
                                    page
                            );

                    stream.setFont(
                            new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                            10
                    );

                    stream.beginText();
                    stream.setLeading(14);
                    stream.newLineAtOffset(
                            40,
                            750
                    );

                    lineCount = 0;
                }

                stream.showText(
                        safeLine
                );

                stream.newLine();

                lineCount++;
            }

            stream.endText();
            stream.close();

            document.save(
                    baos
            );

            return baos.toByteArray();
        }
    }
}