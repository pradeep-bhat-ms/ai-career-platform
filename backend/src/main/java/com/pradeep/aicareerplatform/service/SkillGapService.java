package com.pradeep.aicareerplatform.service;


import com.pradeep.aicareerplatform.config.RoleSkillConfig;
import com.pradeep.aicareerplatform.dto.ResumeExtractionDto;
import com.pradeep.aicareerplatform.dto.SkillGapItemDto;
import com.pradeep.aicareerplatform.dto.SkillGapOverviewDto;
import com.pradeep.aicareerplatform.entity.*;
import com.pradeep.aicareerplatform.repository.*;
import com.pradeep.aicareerplatform.util.SkillMatchUtil;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class SkillGapService {

    private final ResumeRepository resumeRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewEvaluationRepository interviewEvaluationRepository;
    private final SkillGapRepository skillGapRepository;
    private final RoleSkillConfig roleSkillConfig;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public SkillGapService(ResumeRepository resumeRepository,
                           InterviewSessionRepository interviewSessionRepository,
                           InterviewQuestionRepository interviewQuestionRepository,
                           InterviewEvaluationRepository interviewEvaluationRepository,
                           SkillGapRepository skillGapRepository,
                           RoleSkillConfig roleSkillConfig,
                           UserRepository userRepository,
                           ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.interviewQuestionRepository = interviewQuestionRepository;
        this.interviewEvaluationRepository = interviewEvaluationRepository;
        this.skillGapRepository = skillGapRepository;
        this.roleSkillConfig = roleSkillConfig;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public SkillGapOverviewDto computeSkillGap(Long resumeId, String targetRole, String userEmail) throws Exception {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        if (!resume.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You do not have access to this resume");
        }
        if (resume.getExtractedDataJson() == null) {
            throw new IllegalStateException("Resume must be analyzed first");
        }

        ResumeExtractionDto resumeData = objectMapper.readValue(resume.getExtractedDataJson(), ResumeExtractionDto.class);
        List<String> resumeSkills = resumeData.getTechnicalSkills();

        RoleSkillConfig.RoleSkills roleSkills = roleSkillConfig.getSkillsForRole(targetRole);

        double interviewAverage = getRecentInterviewAverage(user.getId(), targetRole);

        List<SkillGapItemDto> gapItems = new ArrayList<>();
        skillGapRepository.deleteAll(skillGapRepository.findByUserIdAndTargetRole(user.getId(), targetRole));

        for (String skill : roleSkills.required()) {
            String status = computeStatus(resumeSkills, skill, interviewAverage, true);
            gapItems.add(new SkillGapItemDto(skill, status));
            saveSkillGap(user, skill, status, targetRole);
        }

        for (String skill : roleSkills.recommended()) {
            String status = computeStatus(resumeSkills, skill, interviewAverage, false);
            gapItems.add(new SkillGapItemDto(skill, status));
            saveSkillGap(user, skill, status, targetRole);
        }

        return new SkillGapOverviewDto(targetRole, gapItems);
    }

    private String computeStatus(List<String> resumeSkills, String skill, double interviewAverage, boolean isRequired) {
        boolean inResume = SkillMatchUtil.matches(resumeSkills, skill);

        if (!inResume) {
            return "Missing";
        }
        // Skill is in resume — refine status using interview performance if available
        if (interviewAverage >= 0) {
            if (interviewAverage >= 7.0) return "Strong";
            if (interviewAverage >= 4.0) return "Medium";
            return "Weak";
        }
        // No interview data yet — default to Medium if present in resume, Strong if it's a core required skill
        return isRequired ? "Medium" : "Strong";
    }

    private double getRecentInterviewAverage(Long userId, String targetRole) {
        List<InterviewSession> sessions = interviewSessionRepository.findByUserId(userId).stream()
                .filter(s -> s.getRole().equalsIgnoreCase(targetRole) && "COMPLETED".equals(s.getStatus()))
                .toList();

        if (sessions.isEmpty()) {
            return -1; // no data
        }

        InterviewSession latest = sessions.get(sessions.size() - 1);
        List<InterviewQuestion> questions = interviewQuestionRepository.findBySessionIdOrderByQuestionOrder(latest.getId());

        double totalScore = 0;
        int count = 0;
        for (InterviewQuestion q : questions) {
            var evaluation = interviewEvaluationRepository.findAll().stream()
                    .filter(e -> e.getAnswer().getQuestion().getId().equals(q.getId()))
                    .findFirst();
            if (evaluation.isPresent()) {
                totalScore += evaluation.get().getScore();
                count++;
            }
        }
        return count == 0 ? -1 : totalScore / count;
    }

    private void saveSkillGap(User user, String skillName, String status, String targetRole) {
        SkillGap gap = new SkillGap();
        gap.setUser(user);
        gap.setSkillName(skillName);
        gap.setStatus(status);
        gap.setTargetRole(targetRole);
        skillGapRepository.save(gap);
    }
}