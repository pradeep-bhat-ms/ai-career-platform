package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.LearningItemDto;
import com.pradeep.aicareerplatform.dto.LearningPlanResponseDto;
import com.pradeep.aicareerplatform.dto.SkillGapItemDto;
import com.pradeep.aicareerplatform.dto.SkillGapOverviewDto;
import com.pradeep.aicareerplatform.entity.LearningItem;
import com.pradeep.aicareerplatform.entity.LearningPlan;
import com.pradeep.aicareerplatform.entity.User;
import com.pradeep.aicareerplatform.repository.LearningItemRepository;
import com.pradeep.aicareerplatform.repository.LearningPlanRepository;
import com.pradeep.aicareerplatform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LearningPlanService {

    private final SkillGapService skillGapService;
    private final LearningPlanAiService learningPlanAiService;
    private final LearningPlanRepository learningPlanRepository;
    private final LearningItemRepository learningItemRepository;
    private final UserRepository userRepository;

    public LearningPlanService(SkillGapService skillGapService,
                               LearningPlanAiService learningPlanAiService,
                               LearningPlanRepository learningPlanRepository,
                               LearningItemRepository learningItemRepository,
                               UserRepository userRepository) {
        this.skillGapService = skillGapService;
        this.learningPlanAiService = learningPlanAiService;
        this.learningPlanRepository = learningPlanRepository;
        this.learningItemRepository = learningItemRepository;
        this.userRepository = userRepository;
    }

    public LearningPlanResponseDto generatePlan(Long resumeId, String targetRole, String userEmail) throws Exception {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SkillGapOverviewDto gapOverview = skillGapService.computeSkillGap(resumeId, targetRole, userEmail);

        List<String> missingSkills = gapOverview.getSkillGaps().stream()
                .filter(g -> "Missing".equals(g.getStatus()))
                .map(SkillGapItemDto::getSkillName)
                .collect(Collectors.toList());

        List<String> weakSkills = gapOverview.getSkillGaps().stream()
                .filter(g -> "Weak".equals(g.getStatus()))
                .map(SkillGapItemDto::getSkillName)
                .collect(Collectors.toList());

        LearningItemDto[] generatedItems = learningPlanAiService.generatePlan(targetRole, missingSkills, weakSkills);

        LearningPlan plan = new LearningPlan();
        plan.setUser(user);
        plan.setTargetRole(targetRole);
        learningPlanRepository.save(plan);

        List<LearningItemDto> responseItems = new ArrayList<>();
        for (LearningItemDto itemDto : generatedItems) {
            LearningItem item = new LearningItem();
            item.setLearningPlan(plan);
            item.setWeekNumber(itemDto.getWeekNumber());
            item.setTopic(itemDto.getTopic());
            item.setResourceSuggestion(itemDto.getResourceSuggestion());
            learningItemRepository.save(item);
            responseItems.add(itemDto);
        }

        return new LearningPlanResponseDto(plan.getId(), targetRole, responseItems);
    }
}