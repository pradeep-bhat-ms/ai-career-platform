package com.pradeep.aicareerplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartInterviewRequestDto {

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Interview type is required")
    private String interviewType;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    @NotNull(message = "Number of questions is required")
    private Integer totalQuestions;
}