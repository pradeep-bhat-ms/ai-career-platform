package com.pradeep.aicareerplatform.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoleAnalysisAiService {

    private final ChatClient chatClient;

    public RoleAnalysisAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateSuggestions(String targetRole, List<String> matchedSkills,
                                      List<String> missingRequired, List<String> missingRecommended) {

        String promptText = """
                You are a senior technical career advisor.

                Target role: %s
                Matched candidate skills: %s
                Missing REQUIRED skills: %s
                Missing RECOMMENDED skills: %s

                Instructions:
                1. Provide 2-3 specific, actionable recommendations on what to learn or highlight next.
                2. Prioritize missing REQUIRED skills first before recommended skills.
                3. Do not invent or reference skills not present in the missing lists.
                4. Keep the total response concise (3-4 sentences maximum).
                """.formatted(targetRole, matchedSkills, missingRequired, missingRecommended);

        return chatClient.prompt()
                .user(promptText)
                .call()
                .content();
    }
}