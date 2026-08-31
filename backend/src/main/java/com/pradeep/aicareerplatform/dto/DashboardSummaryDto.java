package com.pradeep.aicareerplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class DashboardSummaryDto {
    private int resumeCount;
    private int jobDescriptionCount;
    private int documentCount;
    private int completedInterviewCount;
    private Double averageInterviewScore; // null if no completed interviews
    private List<Integer> interviewScoreTrend; // score per completed session, in order
    private Map<String, Long> skillGapBreakdown; // Strong/Medium/Weak/Missing -> count
    private Integer latestMatchPercentage; // null if no matches computed yet
}