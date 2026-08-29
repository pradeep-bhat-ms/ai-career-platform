package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.*;
import com.pradeep.aicareerplatform.entity.CodeChallenge;
import com.pradeep.aicareerplatform.entity.CodeSubmission;
import com.pradeep.aicareerplatform.entity.User;
import com.pradeep.aicareerplatform.repository.CodeChallengeRepository;
import com.pradeep.aicareerplatform.repository.CodeSubmissionRepository;
import com.pradeep.aicareerplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CodeChallengeService {

    private final CodeChallengeRepository challengeRepository;
    private final CodeSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final CodeChallengeAiService codeChallengeAiService;

    public CodeChallengeService(CodeChallengeRepository challengeRepository,
                                CodeSubmissionRepository submissionRepository,
                                UserRepository userRepository,
                                CodeChallengeAiService codeChallengeAiService) {
        this.challengeRepository = challengeRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.codeChallengeAiService = codeChallengeAiService;
    }

    // 1. Dynamic AI Question Generator
    public GeneratedProblemDto generateDynamicProblem(GenerateQuestionRequestDto request) {
        return codeChallengeAiService.generateProblem(request);
    }

    // 2. Strict LeetCode Compiler & Test-Case Judge
    @Transactional
    public JudgeResultDto judgeAndSaveSubmission(JudgeCodeRequestDto request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Run AI evaluation against test cases
        JudgeResultDto result = codeChallengeAiService.judgeCode(request);

        // Record submission telemetry
        CodeSubmission submission = new CodeSubmission();
        submission.setUser(user);
        submission.setProblemTitle(request.getProblemTitle()); // Stores the dynamic question title
        submission.setLanguage(request.getLanguage());
        submission.setSubmittedCode(request.getCode());
        submission.setVerdict(result.getVerdict());
        submission.setScore(result.isPassed() ? 100 : 0);
        submission.setTimeComplexity(result.getRuntime());
        submission.setSpaceComplexity(result.getMemory());
        submission.setFeedback(result.isPassed() ? result.getStrengths() : result.getErrorOutput());

        submissionRepository.save(submission);

        return result;
    }

    // 3. User Submissions History
    public List<CodeSubmission> getUserSubmissions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return submissionRepository.findByUserIdOrderBySubmittedAtDesc(user.getId());
    }

    // 4. Delete Submission
    @Transactional
    public void deleteSubmission(Long submissionId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        CodeSubmission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        if (!sub.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this submission");
        }
        submissionRepository.delete(sub);
    }

    // 5. Existing Repository Fetchers
    public List<CodeChallenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    public CodeChallenge getChallengeById(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found with ID: " + id));
    }
}