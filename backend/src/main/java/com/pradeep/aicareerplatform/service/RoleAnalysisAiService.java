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
                You are a career advisor helping a candidate improve their resume for a specific role.

                Target role: %s
                Skills the candidate already has: %s
                Missing REQUIRED skills for this role: %s
                Missing RECOMMENDED (not mandatory) skills: %s

                Write 2-3 short, specific, encouraging suggestions for what to learn next, in priority order.
                Prioritize required skills first. Do not describe recommended skills as mandatory.
                Do not mention any skill that isn't in the missing lists above.
                Keep it concise, no more than 4 sentences total.
                """.formatted(targetRole, matchedSkills, missingRequired, missingRecommended);

        return chatClient.prompt()
                .user(promptText)
                .call()
                .content();
    }
}