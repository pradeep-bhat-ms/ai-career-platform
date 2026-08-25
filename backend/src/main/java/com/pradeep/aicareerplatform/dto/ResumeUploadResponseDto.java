package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResumeUploadResponseDto {
    private Long resumeId;
    private String rawText;
    private String message;
}

