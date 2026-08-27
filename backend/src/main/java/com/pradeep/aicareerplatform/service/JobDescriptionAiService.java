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
                You are an expert job description analyzer.

                Extract structured information from the job posting below.

                IMPORTANT RULES:

                1. Only extract information that is explicitly present in the job description.
                   Do not invent or assume anything.

                2. Distinguish clearly between:
                   - requiredSkills: skills explicitly stated as mandatory, required, must-have,
                     or essential.
                   - preferredSkills: skills explicitly stated as preferred, nice-to-have,
                     bonus, added advantage, or optional.

                3. If a requirement is written as a full sentence or phrase, extract ONLY the
                   specific skill or technology keywords from within it, not the complete sentence.

                   Example:
                   "Basic knowledge of SQL and databases"
                   → "SQL"

                   Example:
                   "Basic understanding of APIs, JSON, and XML"
                   → "APIs", "JSON", "XML"

                   Example:
                   "Good analytical and problem-solving skills"
                   → "Analytical Skills", "Problem Solving"

                4. Do NOT extract generic category phrases as skills.

                   Do NOT return phrases such as:
                   - database technologies
                   - programming skills
                   - software development skills
                   - frontend technologies
                   - backend technologies
                   - cloud technologies
                   - development tools

                5. If a generic category is followed by a specific technology, extract
                   the specific technology instead.

                   Example:
                   "SQL and database technologies"
                   → "SQL"

                6. Do not convert a skill into another technology.

                   Example:
                   "SQL" does NOT mean "MySQL".
                   "AWS" does NOT mean "Azure".
                   "React.js" does NOT mean "Angular".

                7. For soft skills, extract them only when they are clearly required
                   competencies for the role. Avoid vague wording such as "good attitude"
                   or "hard working".

                8. Do not duplicate skills.

                9. Keep skill names short and clear.

                10. Extract responsibilities only when they are explicitly described
                    as responsibilities or duties in the job posting.

                Job description:

                %s
                """.formatted(jdText);

        return chatClient.prompt()
                .user(promptText)
                .call()
                .entity(JobDescriptionExtractionDto.class);
    }
}