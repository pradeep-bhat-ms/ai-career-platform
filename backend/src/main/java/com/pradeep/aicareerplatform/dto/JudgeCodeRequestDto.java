package com.pradeep.aicareerplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JudgeCodeRequestDto {
    @NotBlank
    private String problemTitle;
    @NotBlank
    private String problemDescription;
    @NotBlank
    private String language;
    @NotBlank
    private String code;
}