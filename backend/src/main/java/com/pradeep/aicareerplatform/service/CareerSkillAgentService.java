package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.CareerSkillAgentResponseDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CareerSkillAgentService {

    private final ChatClient chatClient;

    public CareerSkillAgentService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public CareerSkillAgentResponseDto analyze(List<String> resumeSkills, String targetRole) {
        String promptText = """
                You are a career skill advisor AI agent.

                Candidate's current skills (from their resume, verified — do not add to this list):
                %s

                Target role: %s

                Your task:
1. From the candidate's skills above, list ONLY the ones directly relevant to succeeding in this specific target role ("alreadyHave"). Exclude generic tools (IDEs, Postman, text editors) and skills unrelated to this role's core responsibilities — only include skills a hiring manager for this role would actually care about.
                2. Identify important skills genuinely missing for this specific role ("importantMissingSkills") — be specific to the role, not generic.
                3. Recommend the most useful next skills to learn, each with:
                   - skill name
                   - a one-sentence reason why it matters for THIS candidate and THIS role
                   - a priority: exactly "High", "Medium", or "Low"

                Rules:
                - Base everything strictly on the candidate's actual listed skills and the target role. Do not invent skills or experience not shown above.
                - Do not recommend unrelated or irrelevant technologies.
                - Keep missing skills specific and genuinely important for this role, not a generic list.
                - Limit recommendedNextSkills to at most 5 items, ordered by priority (High first).
                """.formatted(resumeSkills, targetRole);

        return chatClient.prompt()
                .user(promptText)
                .call()
                .entity(CareerSkillAgentResponseDto.class);
    }
}