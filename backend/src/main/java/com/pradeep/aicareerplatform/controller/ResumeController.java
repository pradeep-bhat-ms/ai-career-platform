package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.ResumeUploadResponseDto;
import com.pradeep.aicareerplatform.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ResumeUploadResponseDto> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        String userEmail = authentication.getName();
        ResumeUploadResponseDto response = resumeService.uploadResume(file, userEmail);
        return ResponseEntity.ok(response);
    }
}