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
                You are a specialized Career Skill AI Agent.

                Candidate verified technical skills: %s
                Target Role: %s

                Tasks:
                1. Identify 'alreadyHave': List ONLY technical skills relevant to succeeding in this target role. Exclude IDEs or text editors.
                2. Identify 'importantMissingSkills': List critical missing skills for this target role.
                3. Generate 'recommendedNextSkills': Provide up to 5 highest-priority skills to learn next, each with a one-sentence rationale and a priority of 'High', 'Medium', or 'Low'.

                Rules:
                - Do not invent skills or work history not shown in the candidate profile.
                - Format response cleanly as structured data matching the CareerSkillAgentResponseDto schema.
                """.formatted(resumeSkills, targetRole);

        return chatClient.prompt()
                .user(promptText)
                .call()
                .entity(CareerSkillAgentResponseDto.class);
    }
}