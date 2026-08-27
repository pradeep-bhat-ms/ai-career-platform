package com.pradeep.aicareerplatform.service;


import com.pradeep.aicareerplatform.dto.JobDescriptionExtractionDto;
import com.pradeep.aicareerplatform.dto.JobDescriptionResponseDto;
import com.pradeep.aicareerplatform.dto.JobDescriptionSubmitRequestDto;
import com.pradeep.aicareerplatform.entity.JobDescription;
import com.pradeep.aicareerplatform.entity.User;
import com.pradeep.aicareerplatform.repository.JobDescriptionRepository;
import com.pradeep.aicareerplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class JobDescriptionService {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final UserRepository userRepository;
    private final JobDescriptionAiService jobDescriptionAiService;
    private final ObjectMapper objectMapper;

    public JobDescriptionService(JobDescriptionRepository jobDescriptionRepository,
                                 UserRepository userRepository,
                                 JobDescriptionAiService jobDescriptionAiService,
                                 ObjectMapper objectMapper) {
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.userRepository = userRepository;
        this.jobDescriptionAiService = jobDescriptionAiService;
        this.objectMapper = objectMapper;
    }

    public JobDescriptionResponseDto submitAndAnalyze(JobDescriptionSubmitRequestDto request, String userEmail) throws Exception {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        JobDescription jd = new JobDescription();
        jd.setUser(user);
        jd.setJobTitle(request.getJobTitle());
        jd.setCompany(request.getCompany());
        jd.setRawText(request.getRawText());

        JobDescriptionExtractionDto extracted = jobDescriptionAiService.extractJobDescriptionData(request.getRawText());

        jd.setExtractedDataJson(objectMapper.writeValueAsString(extracted));
        jobDescriptionRepository.save(jd);

        return new JobDescriptionResponseDto(jd.getId(), jd.getJobTitle(), extracted, "Job description analyzed successfully");
    }
    public List<JobDescription> getJobDescriptionsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return jobDescriptionRepository.findByUserId(user.getId());
    }
    public void deleteJobDescription(Long jobDescriptionId, String userEmail) {
        JobDescription jd = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Job description not found"));

        if (!jd.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You do not have access to this job description");
        }

        jobDescriptionRepository.delete(jd);
    }
}