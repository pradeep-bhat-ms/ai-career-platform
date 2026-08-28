package com.pradeep.aicareerplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RagQueryRequestDto {

    @NotBlank(message = "Question is required")
    private String question;

    private String category; // optional filter, e.g. "Spring Boot"
}