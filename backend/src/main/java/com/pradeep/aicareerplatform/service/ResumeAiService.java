package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.ResumeExtractionDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ResumeAiService {

    private final ChatClient chatClient;

    public ResumeAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public ResumeExtractionDto extractResumeData(String resumeText) {
        String promptText = """
                You are an expert resume parser. Extract structured information from the resume text below.
                Only extract information that is explicitly present in the text. Do not invent or assume anything.

                Resume text:
                %s
                """.formatted(resumeText);

        return chatClient.prompt()
                .user(promptText)
                .call()
                .entity(ResumeExtractionDto.class);
    }
}