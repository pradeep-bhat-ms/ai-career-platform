package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.*;
import com.pradeep.aicareerplatform.entity.CodeSubmission;
import com.pradeep.aicareerplatform.service.CodeChallengeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/code")
public class CodeChallengeController {

    private final CodeChallengeService codeChallengeService;

    public CodeChallengeController(CodeChallengeService codeChallengeService) {
        this.codeChallengeService = codeChallengeService;
    }

    @PostMapping("/generate")
    public ResponseEntity<GeneratedProblemDto> generateProblem(@Valid @RequestBody GenerateQuestionRequestDto req) {
        return ResponseEntity.ok(codeChallengeService.generateDynamicProblem(req));
    }

    @PostMapping("/judge")
    public ResponseEntity<JudgeResultDto> judgeSolution(
            @Valid @RequestBody JudgeCodeRequestDto req,
            Authentication authentication) {
        return ResponseEntity.ok(codeChallengeService.judgeAndSaveSubmission(req, authentication.getName()));
    }

    @GetMapping("/submissions")
    public ResponseEntity<List<CodeSubmission>> getSubmissions(Authentication authentication) {
        return ResponseEntity.ok(codeChallengeService.getUserSubmissions(authentication.getName()));
    }

    @DeleteMapping("/submissions/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id, Authentication authentication) {
        codeChallengeService.deleteSubmission(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}