package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.DashboardSummaryDto;
import com.pradeep.aicareerplatform.entity.*;
import com.pradeep.aicareerplatform.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final DocumentRepository documentRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewEvaluationRepository interviewEvaluationRepository;
    private final SkillGapRepository skillGapRepository;
    private final UserRepository userRepository;

    public DashboardService(ResumeRepository resumeRepository,
                            JobDescriptionRepository jobDescriptionRepository,
                            DocumentRepository documentRepository,
                            InterviewSessionRepository interviewSessionRepository,
                            InterviewQuestionRepository interviewQuestionRepository,
                            InterviewEvaluationRepository interviewEvaluationRepository,
                            SkillGapRepository skillGapRepository,
                            UserRepository userRepository) {
        this.resumeRepository = resumeRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.documentRepository = documentRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.interviewQuestionRepository = interviewQuestionRepository;
        this.interviewEvaluationRepository = interviewEvaluationRepository;
        this.skillGapRepository = skillGapRepository;
        this.userRepository = userRepository;
    }

    public DashboardSummaryDto getSummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Long userId = user.getId();

        int resumeCount = resumeRepository.findByUserId(userId).size();
        int jdCount = jobDescriptionRepository.findByUserId(userId).size();
        int documentCount = documentRepository.findByUserId(userId).size();

        List<InterviewSession> completedSessions = interviewSessionRepository.findByUserId(userId).stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .sorted(Comparator.comparing(InterviewSession::getCompletedAt))
                .collect(Collectors.toList());

        List<Integer> scoreTrend = completedSessions.stream()
                .map(InterviewSession::getFinalScore)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Double averageScore = scoreTrend.isEmpty() ? null :
                scoreTrend.stream().mapToInt(Integer::intValue).average().orElse(0);

        Map<String, Long> skillGapBreakdown = new LinkedHashMap<>();
        skillGapBreakdown.put("Strong", 0L);
        skillGapBreakdown.put("Medium", 0L);
        skillGapBreakdown.put("Weak", 0L);
        skillGapBreakdown.put("Missing", 0L);

        List<SkillGap> allGaps = skillGapRepository.findAll().stream()
                .filter(g -> g.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        // Keep only the latest snapshot per skill (in case of duplicates across roles)
        Map<String, SkillGap> latestPerSkill = new LinkedHashMap<>();
        for (SkillGap gap : allGaps) {
            latestPerSkill.merge(gap.getSkillName() + "|" + gap.getTargetRole(), gap,
                    (existing, incoming) -> incoming.getComputedAt().isAfter(existing.getComputedAt()) ? incoming : existing);
        }

        for (SkillGap gap : latestPerSkill.values()) {
            skillGapBreakdown.merge(gap.getStatus(), 1L, Long::sum);
        }

        return new DashboardSummaryDto(
                resumeCount, jdCount, documentCount,
                completedSessions.size(), averageScore, scoreTrend,
                skillGapBreakdown, null // latestMatchPercentage — see note below
        );
    }
}