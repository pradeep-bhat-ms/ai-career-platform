package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.InterviewQuestionDto;
import com.pradeep.aicareerplatform.dto.StartInterviewRequestDto;
import com.pradeep.aicareerplatform.dto.SubmitAnswerRequestDto;
import com.pradeep.aicareerplatform.dto.SubmitAnswerResponseDto;
import com.pradeep.aicareerplatform.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/start")
    public ResponseEntity<InterviewQuestionDto> startInterview(
            @Valid @RequestBody StartInterviewRequestDto request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        InterviewQuestionDto response = interviewService.startInterview(request, userEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{questionId}/answer")
    public ResponseEntity<SubmitAnswerResponseDto> submitAnswer(
            @PathVariable Long questionId,
            @Valid @RequestBody SubmitAnswerRequestDto request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        SubmitAnswerResponseDto response = interviewService.submitAnswer(questionId, request.getAnswerText(), userEmail);
        return ResponseEntity.ok(response);
    }
}