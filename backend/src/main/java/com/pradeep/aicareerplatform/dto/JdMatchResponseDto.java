package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class JdMatchResponseDto {
    private String jobTitle;
    private List<String> matchedRequiredSkills;
    private List<String> missingRequiredSkills;
    private List<String> matchedPreferredSkills;
    private List<String> missingPreferredSkills;
    private int matchPercentage;
    private String suggestions;
}