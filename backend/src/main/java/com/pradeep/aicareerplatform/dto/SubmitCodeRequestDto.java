package com.pradeep.aicareerplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitCodeRequestDto {

    @NotNull(message = "Challenge ID is required")
    private Long challengeId;

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Submitted code cannot be empty")
    private String code;
}