package com.pradeep.aicareerplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitAnswerRequestDto {

    @NotBlank(message = "Answer is required")
    private String answerText;
}