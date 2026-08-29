package com.pradeep.aicareerplatform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JudgeResultDto {
    private String verdict; // ACCEPTED, WRONG_ANSWER, COMPILATION_ERROR, TIME_LIMIT_EXCEEDED
    private boolean passed;
    private String runtime;
    private String memory;
    private String errorOutput;
    private String strengths;
    private String testCaseDetails;
}