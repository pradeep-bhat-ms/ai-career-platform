package com.pradeep.aicareerplatform.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JdMatchAiService {

    private final ChatClient chatClient;

    public JdMatchAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateSuggestions(String jobTitle, List<String> missingRequired, List<String> missingPreferred) {
        String promptText = """
                You are a career advisor. A candidate is applying for: %s

                Missing REQUIRED skills for this specific job posting: %s
                Missing PREFERRED (not mandatory) skills: %s

                Write 2-3 short, specific, encouraging suggestions for improving their fit for THIS job.
                Prioritize required skills first. Never call preferred skills mandatory.
                Do not mention any skill outside these two lists.
                Keep it under 4 sentences.
                """.formatted(jobTitle, missingRequired, missingPreferred);

        return chatClient.prompt()
                .user(promptText)
                .call()
                .content();
    }
}