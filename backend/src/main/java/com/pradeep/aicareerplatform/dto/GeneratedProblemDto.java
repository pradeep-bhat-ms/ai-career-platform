package com.pradeep.aicareerplatform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneratedProblemDto {
    private String title;
    private String difficulty;
    private String category;
    private String description;
    private String exampleInput;
    private String exampleOutput;
    private String constraints;
    private String starterCode;
}