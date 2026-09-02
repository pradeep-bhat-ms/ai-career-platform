package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.ResumeImprovementSuggestionDto;
import com.pradeep.aicareerplatform.dto.RoleAnalysisResponseDto;
import com.pradeep.aicareerplatform.entity.Resume;
import com.pradeep.aicareerplatform.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

@Service
public class ResumeImprovementService {

    private static final Logger log = LoggerFactory.getLogger(ResumeImprovementService.class);

    private final ChatClient chatClient;
    private final ResumeRepository resumeRepository;
    private final RoleAnalysisService roleAnalysisService;
    private final ObjectMapper objectMapper;

    public ResumeImprovementService(ChatClient.Builder chatClientBuilder,
                                    ResumeRepository resumeRepository,
                                    RoleAnalysisService roleAnalysisService,
                                    ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.resumeRepository = resumeRepository;
        this.roleAnalysisService = roleAnalysisService;
        this.objectMapper = objectMapper;
    }

    public List<ResumeImprovementSuggestionDto> getActionableImprovements(
            Long resumeId,
            String targetRole,
            List<String> missingSkills,
            String sectionFilter,
            String userEmail) {

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        if (resume.getUser() == null || !resume.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("Access denied: You do not have permission to view or improve this resume.");
        }

        String missingSkillsText = (missingSkills == null || missingSkills.isEmpty())
                ? "None"
                : String.join(", ", missingSkills);

        String sectionInstruction = (sectionFilter == null || sectionFilter.equalsIgnoreCase("ALL"))
                ? "Provide 3 to 4 prioritized, balanced suggestions across all sections."
                : "STRICT: Focus ONLY on rewriting the " + sectionFilter + " section. Provide 2 to 3 targeted suggestions exclusively for this section.";

        String prompt = String.format("""
            You are a principal technical career coach.
            Target Role: %s
            Detected Deficiencies in Resume:
            1. Missing Core Skills: %s
            2. Experience duration is unclear or missing metrics.
            3. Project descriptions lack measurable outcomes/technologies.

            Task Scope:
            %s

            Original Resume Text:
            %s

            Rules:
            - Return ONLY a valid JSON array.
            - Do NOT invent companies or metrics the candidate didn't mention.
            - Where possible, show the exact 'originalText' to replace, the 'suggestedText', and the 'reason'.

            Output JSON structure:
            [
              {
                "id": "sug-1",
                "section": "EXPERIENCE",
                "priority": "HIGH",
                "issueTitle": "Clarify Technical Hands-on Experience",
                "originalText": "Fresher / Trainee",
                "suggestedText": "Associate Software Engineer / Trainee with hands-on focus on Java Spring Boot and RESTful service development.",
                "reason": "Clarifies professional capacity for automated ATS filters.",
                "selected": true
              }
            ]
            """, targetRole, missingSkillsText, sectionInstruction, resume.getRawText());

        String response = chatClient.prompt().user(prompt).call().content();
        return parseSuggestionsJson(response);
    }

    @Transactional
    public RoleAnalysisResponseDto applyImprovementsAndReanalyze(
            Long resumeId,
            String targetRole,
            List<ResumeImprovementSuggestionDto> improvements,
            String userEmail) throws Exception {

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        if (resume.getUser() == null || !resume.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("Access denied: You do not have permission to modify this resume.");
        }

        String updatedText = resume.getRawText();
        if (improvements != null) {
            for (ResumeImprovementSuggestionDto imp : improvements) {
                if (imp.getOriginalText() != null && !imp.getOriginalText().isBlank() && updatedText.contains(imp.getOriginalText())) {
                    updatedText = updatedText.replace(imp.getOriginalText(), imp.getSuggestedText());
                }
            }
        }
        resume.setRawText(updatedText);
        resumeRepository.save(resume);

        return roleAnalysisService.analyzeForRole(resumeId, targetRole, userEmail);
    }

    private List<ResumeImprovementSuggestionDto> parseSuggestionsJson(String rawAiResponse) {
        if (rawAiResponse == null || rawAiResponse.isBlank()) {
            return Collections.emptyList();
        }

        try {
            String cleanedJson = rawAiResponse.trim();
            if (cleanedJson.contains("```json")) {
                cleanedJson = cleanedJson.substring(cleanedJson.indexOf("```json") + 7);
            } else if (cleanedJson.contains("```")) {
                cleanedJson = cleanedJson.substring(cleanedJson.indexOf("```") + 3);
            }
            if (cleanedJson.contains("```")) {
                cleanedJson = cleanedJson.substring(0, cleanedJson.lastIndexOf("```"));
            }

            int start = cleanedJson.indexOf('[');
            int end = cleanedJson.lastIndexOf(']');
            if (start != -1 && end != -1 && end > start) {
                cleanedJson = cleanedJson.substring(start, end + 1);
            }

            return objectMapper.readValue(
                    cleanedJson,
                    new TypeReference<List<ResumeImprovementSuggestionDto>>() {}
            );
        } catch (Exception e) {
            log.error("Failed to parse AI resume suggestions JSON. Raw AI Output: {}", rawAiResponse, e);
            return Collections.emptyList();
        }
    }
}