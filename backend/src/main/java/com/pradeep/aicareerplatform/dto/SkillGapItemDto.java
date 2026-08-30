package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SkillGapItemDto {
    private String skillName;
    private String status; // Strong, Medium, Weak, Missing
}