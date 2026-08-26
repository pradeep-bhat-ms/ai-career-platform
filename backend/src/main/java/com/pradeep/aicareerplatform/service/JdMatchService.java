package com.pradeep.aicareerplatform.service;


import com.pradeep.aicareerplatform.dto.JdMatchResponseDto;
import com.pradeep.aicareerplatform.dto.JobDescriptionExtractionDto;
import com.pradeep.aicareerplatform.dto.ResumeExtractionDto;
import com.pradeep.aicareerplatform.entity.JobDescription;
import com.pradeep.aicareerplatform.entity.Resume;
import com.pradeep.aicareerplatform.repository.JobDescriptionRepository;
import com.pradeep.aicareerplatform.repository.ResumeRepository;
import com.pradeep.aicareerplatform.util.SkillMatchUtil;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class JdMatchService {

    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final JdMatchAiService jdMatchAiService;
    private final ObjectMapper objectMapper;

    public JdMatchService(ResumeRepository resumeRepository,
                          JobDescriptionRepository jobDescriptionRepository,
                          JdMatchAiService jdMatchAiService,
                          ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.jdMatchAiService = jdMatchAiService;
        this.objectMapper = objectMapper;
    }

    public JdMatchResponseDto matchResumeToJob(Long resumeId, Long jobDescriptionId, String userEmail) throws Exception {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        JobDescription jd = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Job description not found"));

        if (!resume.getUser().getEmail().equals(userEmail) || !jd.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You do not have access to this resume or job description");
        }

        if (resume.getExtractedDataJson() == null) {
            throw new IllegalStateException("Resume must be analyzed first");
        }

        ResumeExtractionDto resumeData = objectMapper.readValue(resume.getExtractedDataJson(), ResumeExtractionDto.class);
        JobDescriptionExtractionDto jdData = objectMapper.readValue(jd.getExtractedDataJson(), JobDescriptionExtractionDto.class);

        List<String> resumeSkills = resumeData.getTechnicalSkills();

        List<String> matchedRequired = new ArrayList<>();
        List<String> missingRequired = new ArrayList<>();
        for (String skill : jdData.getRequiredSkills()) {
            if (SkillMatchUtil.matches(resumeSkills, skill)) {
                matchedRequired.add(skill);
            } else {
                missingRequired.add(skill);
            }
        }

        List<String> matchedPreferred = new ArrayList<>();
        List<String> missingPreferred = new ArrayList<>();
        for (String skill : jdData.getPreferredSkills()) {
            if (SkillMatchUtil.matches(resumeSkills, skill)) {
                matchedPreferred.add(skill);
            } else {
                missingPreferred.add(skill);
            }
        }

        int totalConsidered = jdData.getRequiredSkills().size() + jdData.getPreferredSkills().size();
        int matchedCount = matchedRequired.size() + matchedPreferred.size();
        int matchPercentage = totalConsidered == 0 ? 0 : (matchedCount * 100) / totalConsidered;

        String suggestions = jdMatchAiService.generateSuggestions(jd.getJobTitle(), missingRequired, missingPreferred);

        return new JdMatchResponseDto(jd.getJobTitle(), matchedRequired, missingRequired, matchedPreferred, missingPreferred, matchPercentage, suggestions);
    }
}