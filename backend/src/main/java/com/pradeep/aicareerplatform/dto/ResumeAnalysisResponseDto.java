package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResumeAnalysisResponseDto {
    private Long resumeId;
    private ResumeExtractionDto extractedData;
    private String message;
}