package com.pradeep.aicareerplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateQuestionRequestDto {
    @NotBlank
    private String language; // java, python, javascript, cpp, sql
    @NotBlank
    private String difficulty; // Easy, Medium, Hard
    private String topic; // Arrays, Strings, Trees, Dynamic Programming, SQL, etc.
}