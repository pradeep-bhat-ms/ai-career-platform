package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentUploadResponseDto {
    private Long documentId;
    private String title;
    private String status;
    private int chunkCount;
    private String message;
}