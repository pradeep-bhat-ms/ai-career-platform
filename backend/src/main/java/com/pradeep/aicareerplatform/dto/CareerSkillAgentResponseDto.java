package com.pradeep.aicareerplatform.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CareerSkillAgentResponseDto {
    private List<String> alreadyHave;
    private List<String> importantMissingSkills;
    private List<RecommendedSkillDto> recommendedNextSkills;
}