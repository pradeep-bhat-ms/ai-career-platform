package com.pradeep.aicareerplatform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerEvaluationDto {
    private int score; // 0-10
    private String strengths;
    private String weaknesses;
}