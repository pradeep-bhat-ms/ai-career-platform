package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.AnswerEvaluationDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InterviewAiService {

    private final ChatClient chatClient;

    public InterviewAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateQuestion(String role, String interviewType, String difficulty,
                                   List<String> previousQuestions, int questionNumber, int totalQuestions) {

        String promptText = """
                You are conducting a %s interview for a %s role at %s difficulty.
                This is question %d of %d.

                Questions already asked in this session (do not repeat these topics):
                %s

                Generate ONE clear, specific interview question appropriate for this role, type, and difficulty.
                Return ONLY the question text, nothing else — no numbering, no preamble.
                """.formatted(interviewType, role, difficulty, questionNumber, totalQuestions,
                previousQuestions.isEmpty() ? "None yet" : String.join("; ", previousQuestions));

        return chatClient.prompt()
                .user(promptText)
                .call()
                .content()
                .trim();
    }

    public AnswerEvaluationDto evaluateAnswer(String role, String question, String answer) {
        String promptText = """
                You are an expert technical interviewer evaluating a candidate's answer.

                Role: %s
                Question: %s
                Candidate's answer: %s

                Evaluate the answer and provide:
                - score: an integer from 0 to 10 based on correctness, completeness, and clarity
                - strengths: what the candidate got right (1-2 sentences, or "None identified" if the answer is entirely wrong)
                - weaknesses: what was missing, incorrect, or could be improved (1-2 sentences, or "None" if the answer is excellent)

                Base your evaluation strictly on the actual content of the answer. Do not be overly harsh or overly lenient.
                """.formatted(role, question, answer);

        return chatClient.prompt()
                .user(promptText)
                .call()
                .entity(AnswerEvaluationDto.class);
    }
}