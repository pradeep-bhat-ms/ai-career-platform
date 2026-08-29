package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InterviewQuestionDto {
    private Long sessionId;
    private Long questionId;
    private String questionText;
    private int questionNumber;
    private int totalQuestions;
}