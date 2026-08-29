package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubmitAnswerResponseDto {
    private int score;
    private String strengths;
    private String weaknesses;
    private boolean sessionComplete;
    private InterviewQuestionDto nextQuestion; // null if session complete
}