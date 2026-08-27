package com.pradeep.aicareerplatform.service;


import com.pradeep.aicareerplatform.dto.CareerSkillAgentResponseDto;
import com.pradeep.aicareerplatform.dto.ResumeAnalysisResponseDto;
import com.pradeep.aicareerplatform.dto.ResumeExtractionDto;
import com.pradeep.aicareerplatform.dto.ResumeUploadResponseDto;
import com.pradeep.aicareerplatform.entity.Resume;
import com.pradeep.aicareerplatform.entity.User;
import com.pradeep.aicareerplatform.repository.ResumeRepository;
import com.pradeep.aicareerplatform.repository.UserRepository;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.document.Document;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ResumeAiService resumeAiService;
    private final ObjectMapper objectMapper;
    private final CareerSkillAgentService careerSkillAgentService;



    public ResumeService(ResumeRepository resumeRepository,
                         UserRepository userRepository,
                         ResumeAiService resumeAiService,
                         ObjectMapper objectMapper, CareerSkillAgentService careerSkillAgentService) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.resumeAiService = resumeAiService;
        this.objectMapper = objectMapper;
        this.careerSkillAgentService = careerSkillAgentService;
    }

    public ResumeUploadResponseDto uploadResume(MultipartFile file, String userEmail) throws IOException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                new InputStreamResource(file.getInputStream())
        );

        List<Document> documents = pdfReader.get();
        String rawText = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setRawText(rawText);
        resume.setFileName(file.getOriginalFilename());
        resumeRepository.save(resume);

        return new ResumeUploadResponseDto(resume.getId(), rawText, "Resume uploaded successfully");
    }

    public ResumeAnalysisResponseDto analyzeResume(Long resumeId, String userEmail) throws Exception {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        if (!resume.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You do not have access to this resume");
        }

        ResumeExtractionDto extracted = resumeAiService.extractResumeData(resume.getRawText());

        resume.setExtractedDataJson(objectMapper.writeValueAsString(extracted));
        resumeRepository.save(resume);

        return new ResumeAnalysisResponseDto(resume.getId(), extracted, "Resume analyzed successfully");
    }


    public CareerSkillAgentResponseDto runCareerSkillAgent(Long resumeId, String targetRole, String userEmail) throws Exception {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        if (!resume.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You do not have access to this resume");
        }

        if (resume.getExtractedDataJson() == null) {
            throw new IllegalStateException("Resume must be analyzed first");
        }

        ResumeExtractionDto extracted = objectMapper.readValue(resume.getExtractedDataJson(), ResumeExtractionDto.class);

        return careerSkillAgentService.analyze(extracted.getTechnicalSkills(), targetRole);
    }
    public List<Resume> getResumesForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return resumeRepository.findByUserId(user.getId());
    }
    public void deleteResume(Long resumeId, String userEmail) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        if (!resume.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You do not have access to this resume");
        }

        resumeRepository.delete(resume);
    }
}