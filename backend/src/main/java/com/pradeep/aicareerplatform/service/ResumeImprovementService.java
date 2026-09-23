package com.pradeep.aicareerplatform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pradeep.aicareerplatform.dto.ResumeImprovementSuggestionDto;
import com.pradeep.aicareerplatform.dto.RoleAnalysisResponseDto;
import com.pradeep.aicareerplatform.entity.Resume;
import com.pradeep.aicareerplatform.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeImprovementService {

    private static final Logger log = LoggerFactory.getLogger(ResumeImprovementService.class);

    private final ChatClient chatClient;
    private final ResumeRepository resumeRepository;
    private final RoleAnalysisService roleAnalysisService;
    private final ResumeService resumeService; // Added dependency
    private final ObjectMapper objectMapper;

    public ResumeImprovementService(ChatClient.Builder chatClientBuilder,
                                    ResumeRepository resumeRepository,
                                    RoleAnalysisService roleAnalysisService,
                                    @Lazy ResumeService resumeService, // @Lazy prevents circular dependency issues
                                    ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.resumeRepository = resumeRepository;
        this.roleAnalysisService = roleAnalysisService;
        this.resumeService = resumeService;
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
            You are a principal technical career coach and ATS optimization specialist.
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
            - 'originalText' MUST be an exact substring present in the provided Original Resume Text.

            Output JSON structure:
            [
              {
                "id": "sug-1",
                "section": "EXPERIENCE",
                "priority": "HIGH",
                "issueTitle": "Clarify Technical Hands-on Experience",
                "originalText": "<EXACT_ORIGINAL_SUBSTRING>",
                "suggestedText": "<IMPROVED_REWRITE>",
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
            Long parentResumeId,
            String targetRole,
            List<ResumeImprovementSuggestionDto> improvements,
            String userEmail) throws Exception {

        Resume parentResume = resumeRepository.findById(parentResumeId)
                .orElseThrow(() -> new IllegalArgumentException("Original Resume not found"));

        if (parentResume.getUser() == null || !parentResume.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("Access denied: You do not have permission to modify this resume.");
        }

        String updatedText = parentResume.getRawText();

        if (improvements != null) {
            for (ResumeImprovementSuggestionDto imp : improvements) {
                if (imp.getSelected() != null && !imp.getSelected()) {
                    continue;
                }

                if (imp.getOriginalText() != null && !imp.getOriginalText().isBlank()) {
                    String targetStr = imp.getOriginalText().trim();
                    String replacementStr = imp.getSuggestedText() == null ? "" : imp.getSuggestedText().trim();

                    if (updatedText.contains(targetStr)) {
                        updatedText = updatedText.replace(targetStr, replacementStr);
                    } else {
                        String regex = buildFlexibleWhitespaceRegex(targetStr);
                        updatedText = updatedText.replaceAll(regex, Matcher.quoteReplacement(replacementStr));
                    }
                }
            }
        }

        // Save as a new version entry while retaining parent history
        Resume versionedResume = new Resume();
        versionedResume.setUser(parentResume.getUser());
        versionedResume.setFileName(parentResume.getFileName());
        versionedResume.setRawText(updatedText);
        versionedResume.setVersion((parentResume.getVersion() == null ? 0 : parentResume.getVersion()) + 1);
        versionedResume.setParentResumeId(parentResume.getId());
        versionedResume.setUploadedAt(LocalDateTime.now());

        Resume savedVersion = resumeRepository.save(versionedResume);

        // FIX: Extract base data for the new versioned resume so extractedDataJson is populated
        resumeService.analyzeResume(savedVersion.getId(), userEmail);

        // Perform role analysis after base extraction is complete;
        // resumeId is now set inside analyzeForRole() itself via the DTO constructor.
        return roleAnalysisService.analyzeForRole(savedVersion.getId(), targetRole, userEmail);

    }

    /**
     * Builds a regex that matches targetStr with flexible whitespace between words,
     * while treating every word itself as a literal (safe against regex metacharacters).
     * Pattern.quote() cannot be applied to the whole string and then edited afterward,
     * because text inside a \Q...\E block is literal — inserting \s+ there does nothing.
     */
    private String buildFlexibleWhitespaceRegex(String targetStr) {
        String[] words = targetStr.trim().split("\\s+");
        StringBuilder regexBuilder = new StringBuilder("\\b");
        for (int i = 0; i < words.length; i++) {
            regexBuilder.append(Pattern.quote(words[i]));
            if (i < words.length - 1) {
                regexBuilder.append("\\s+");
            }
        }
        regexBuilder.append("\\b");
        return regexBuilder.toString();
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