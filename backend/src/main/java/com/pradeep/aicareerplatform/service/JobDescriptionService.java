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
}