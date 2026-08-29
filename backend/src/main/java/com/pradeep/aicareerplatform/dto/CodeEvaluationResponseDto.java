package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeEvaluationResponseDto {
    private String verdict; // PASSED, FAILED
    private int score; // 0 - 100
    private String timeComplexity;
    private String spaceComplexity;
    private String strengths;
    private String weaknesses;
    private String optimizationSuggestions;
    private String rawAnalysis;
}