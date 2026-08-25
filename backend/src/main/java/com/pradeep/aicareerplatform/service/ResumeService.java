package com.pradeep.aicareerplatform.service;

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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public ResumeService(ResumeRepository resumeRepository, UserRepository userRepository) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
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
        resumeRepository.save(resume);

        return new ResumeUploadResponseDto(resume.getId(), rawText, "Resume uploaded successfully");
    }
}