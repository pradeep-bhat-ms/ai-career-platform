package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class RoleAnalysisResponseDto {
    private String targetRole;
    private List<String> matchedSkills;
    private List<String> missingRequiredSkills;
    private List<String> missingRecommendedSkills;
    private List<String> requiredSkills;
    private List<String> recommendedSkills;
    private List<String> optionalSkills;
    private int matchPercentage;
    private String suggestions;
    private String scoreLabel; // e.g. "AI Resume Compatibility Estimate"
    private List<ScoreCategoryDto> scoreBreakdown;
    private String scoreExplanation; // plain-language "why this number" summary
}