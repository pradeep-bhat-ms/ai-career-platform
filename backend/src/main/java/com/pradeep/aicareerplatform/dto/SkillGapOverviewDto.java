package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class SkillGapOverviewDto {
    private String targetRole;
    private List<SkillGapItemDto> skillGaps;
}