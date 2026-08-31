package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class LearningPlanResponseDto {
    private Long planId;
    private String targetRole;
    private List<LearningItemDto> items;
}