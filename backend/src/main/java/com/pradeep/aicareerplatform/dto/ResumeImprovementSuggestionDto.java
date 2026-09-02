package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeImprovementSuggestionDto {
    private String id;
    private String section;          // SKILLS, EXPERIENCE, PROJECTS, SUMMARY
    private String priority;         // HIGH, MEDIUM, LOW
    private String issueTitle;
    private String originalText;
    private String suggestedText;
    private String reason;
    private boolean selected;
}