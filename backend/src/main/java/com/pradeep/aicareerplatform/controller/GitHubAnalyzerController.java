package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.GitHubAnalysisDto;
import com.pradeep.aicareerplatform.service.GitHubAnalyzerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/github")
public class GitHubAnalyzerController {

    private final GitHubAnalyzerService gitHubAnalyzerService;

    public GitHubAnalyzerController(GitHubAnalyzerService gitHubAnalyzerService) {
        this.gitHubAnalyzerService = gitHubAnalyzerService;
    }

    public record AnalyzeRequest(String profileUrl) {}

    @PostMapping("/analyze")
    public ResponseEntity<GitHubAnalysisDto> analyze(
            @RequestBody AnalyzeRequest request,
            Authentication authentication) {

        // Requires login (consistent with the rest of the app) even though
        // the GitHub data itself is public — prevents this becoming an
        // unauthenticated proxy that anyone could hammer to burn through
        // our shared 60-req/hour GitHub rate limit.
        GitHubAnalysisDto response = gitHubAnalyzerService.analyzeProfile(request.profileUrl());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(GitHubAnalyzerService.InvalidGitHubUrlException.class)
    public ResponseEntity<Map<String, String>> handleInvalidUrl(GitHubAnalyzerService.InvalidGitHubUrlException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(com.pradeep.aicareerplatform.service.GitHubApiService.GitHubUserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(
            com.pradeep.aicareerplatform.service.GitHubApiService.GitHubUserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(com.pradeep.aicareerplatform.service.GitHubApiService.GitHubRateLimitException.class)
    public ResponseEntity<Map<String, String>> handleRateLimit(
            com.pradeep.aicareerplatform.service.GitHubApiService.GitHubRateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(com.pradeep.aicareerplatform.service.GitHubApiService.GitHubApiException.class)
    public ResponseEntity<Map<String, String>> handleApiError(
            com.pradeep.aicareerplatform.service.GitHubApiService.GitHubApiException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", ex.getMessage()));
    }
}