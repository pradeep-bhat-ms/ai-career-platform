package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.JobDescriptionExtractionDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class JobDescriptionAiService {

    private final ChatClient chatClient;

    public JobDescriptionAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public JobDescriptionExtractionDto extractJobDescriptionData(String jdText) {
        String promptText = """
                You are an expert job description analyzer. Extract structured information from the job posting below.
                Only extract information that is explicitly present in the text. Do not invent or assume anything.

                Distinguish clearly between:
                - requiredSkills: skills explicitly stated as mandatory/required
                - preferredSkills: skills explicitly stated as nice-to-have/preferred/bonus

                If the posting doesn't clearly separate required vs preferred, use your best judgment based on the language used (e.g. "must have" = required, "nice to have" = preferred).

                Job description text:
                %s
                """.formatted(jdText);

        return chatClient.prompt()
                .user(promptText)
                .call()
                .entity(JobDescriptionExtractionDto.class);
    }
}