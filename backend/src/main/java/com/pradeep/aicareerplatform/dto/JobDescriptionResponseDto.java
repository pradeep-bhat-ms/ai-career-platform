package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JobDescriptionResponseDto {
    private Long jobDescriptionId;
    private String jobTitle;
    private JobDescriptionExtractionDto extractedData;
    private String message;
}