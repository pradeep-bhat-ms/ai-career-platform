package com.pradeep.aicareerplatform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecommendedSkillDto {
    private String skill;
    private String reason;
    private String priority; // High / Medium / Low
}