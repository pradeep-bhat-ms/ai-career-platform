package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.*;
import com.pradeep.aicareerplatform.entity.*;
import com.pradeep.aicareerplatform.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final InterviewAnswerRepository answerRepository;
    private final InterviewEvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final InterviewAiService interviewAiService;

    public InterviewService(InterviewSessionRepository sessionRepository,
                            InterviewQuestionRepository questionRepository,
                            InterviewAnswerRepository answerRepository,
                            InterviewEvaluationRepository evaluationRepository,
                            UserRepository userRepository,
                            InterviewAiService interviewAiService) {
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.evaluationRepository = evaluationRepository;
        this.userRepository = userRepository;
        this.interviewAiService = interviewAiService;
    }

    public InterviewQuestionDto startInterview(StartInterviewRequestDto request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        InterviewSession session = new InterviewSession();
        session.setUser(user);
        session.setRole(request.getRole());
        session.setInterviewType(request.getInterviewType());
        session.setDifficulty(request.getDifficulty());
        session.setTotalQuestions(request.getTotalQuestions());
        sessionRepository.save(session);

        return generateAndSaveNextQuestion(session, List.of());
    }

    public SubmitAnswerResponseDto submitAnswer(Long questionId, String answerText, String userEmail) {
        InterviewQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        InterviewSession session = question.getSession();
        if (!session.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You do not have access to this interview session");
        }

        InterviewAnswer answer = new InterviewAnswer();
        answer.setQuestion(question);
        answer.setAnswerText(answerText);
        answerRepository.save(answer);

        AnswerEvaluationDto evaluationResult = interviewAiService.evaluateAnswer(
                session.getRole(), question.getQuestionText(), answerText);

        InterviewEvaluation evaluation = new InterviewEvaluation();
        evaluation.setAnswer(answer);
        evaluation.setScore(evaluationResult.getScore());
        evaluation.setStrengths(evaluationResult.getStrengths());
        evaluation.setWeaknesses(evaluationResult.getWeaknesses());
        evaluationRepository.save(evaluation);

        List<InterviewQuestion> askedQuestions = questionRepository.findBySessionIdOrderByQuestionOrder(session.getId());

        if (askedQuestions.size() >= session.getTotalQuestions()) {
            completeSession(session);
            return new SubmitAnswerResponseDto(
                    evaluationResult.getScore(), evaluationResult.getStrengths(), evaluationResult.getWeaknesses(),
                    true, null);
        }

        List<String> previousQuestionTexts = askedQuestions.stream()
                .map(InterviewQuestion::getQuestionText)
                .collect(Collectors.toList());

        InterviewQuestionDto nextQuestion = generateAndSaveNextQuestion(session, previousQuestionTexts);

        return new SubmitAnswerResponseDto(
                evaluationResult.getScore(), evaluationResult.getStrengths(), evaluationResult.getWeaknesses(),
                false, nextQuestion);
    }

    private InterviewQuestionDto generateAndSaveNextQuestion(InterviewSession session, List<String> previousQuestions) {
        int nextQuestionNumber = previousQuestions.size() + 1;

        String questionText = interviewAiService.generateQuestion(
                session.getRole(), session.getInterviewType(), session.getDifficulty(),
                previousQuestions, nextQuestionNumber, session.getTotalQuestions());

        InterviewQuestion question = new InterviewQuestion();
        question.setSession(session);
        question.setQuestionText(questionText);
        question.setQuestionOrder(nextQuestionNumber);
        questionRepository.save(question);

        return new InterviewQuestionDto(session.getId(), question.getId(), questionText,
                nextQuestionNumber, session.getTotalQuestions());
    }

    private void completeSession(InterviewSession session) {
        List<InterviewQuestion> questions = questionRepository.findBySessionIdOrderByQuestionOrder(session.getId());
        int totalScore = questions.stream()
                .mapToInt(q -> evaluationRepository.findAll().stream()
                        .filter(e -> e.getAnswer().getQuestion().getId().equals(q.getId()))
                        .findFirst()
                        .map(InterviewEvaluation::getScore)
                        .orElse(0))
                .sum();

        session.setFinalScore(totalScore);
        session.setStatus("COMPLETED");
        session.setCompletedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }
}