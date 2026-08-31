package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.LearningItemDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LearningPlanAiService {

    private final ChatClient chatClient;

    public LearningPlanAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public LearningItemDto[] generatePlan(String targetRole, List<String> missingSkills, List<String> weakSkills) {
        String promptText = """
                Create a personalized week-by-week learning plan for someone targeting a %s role.

                Missing skills (highest priority): %s
                Weak skills (needs improvement): %s

                Rules:
                - Only include topics from the missing and weak skills listed above. Do not add unrelated topics.
                - Order by priority: missing skills first, then weak skills.
                - Group related skills into the same week if it makes sense (e.g. Docker + Kubernetes together).
                - Create between 3 and 6 weeks total, one primary topic per week.
                - For each week, suggest a general type of resource (e.g. "official documentation", "hands-on project", "video course") — do not invent specific URLs or course names.
                """.formatted(targetRole,
                missingSkills.isEmpty() ? "None" : String.join(", ", missingSkills),
                weakSkills.isEmpty() ? "None" : String.join(", ", weakSkills));

        return chatClient.prompt()
                .user(promptText)
                .call()
                .entity(LearningItemDto[].class);
    }
}