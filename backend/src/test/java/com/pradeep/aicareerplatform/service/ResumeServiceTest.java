package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.entity.Resume;
import com.pradeep.aicareerplatform.entity.User;
import com.pradeep.aicareerplatform.repository.ResumeRepository;
import com.pradeep.aicareerplatform.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResumeAiService resumeAiService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CareerSkillAgentService careerSkillAgentService;

    @InjectMocks
    private ResumeService resumeService;

    private User owner;
    private User anotherUser;
    private Resume resume;

    @BeforeEach
    void setUp() {

        owner = new User();
        owner.setEmail("owner@example.com");
        owner.setFullName("Owner User");

        anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setFullName("Another User");

        resume = new Resume();
        resume.setId(1L);
        resume.setUser(owner);
        resume.setFileName("resume.pdf");
        resume.setRawText("Java Spring Boot Resume");
    }

    @Test
    void deleteResume_allowsOwnerToDeleteOwnResume() {

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        resumeService.deleteResume(
                1L,
                "owner@example.com"
        );

        verify(resumeRepository).delete(resume);
    }

    @Test
    void deleteResume_rejectsUserTryingToDeleteAnotherUsersResume() {

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        assertThatThrownBy(() ->
                resumeService.deleteResume(
                        1L,
                        "another@example.com"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You do not have access to this resume");

        verify(resumeRepository, never()).delete(any(Resume.class));
    }

    @Test
    void analyzeResume_rejectsUserTryingToAnalyzeAnotherUsersResume() {

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        assertThatThrownBy(() ->
                resumeService.analyzeResume(
                        1L,
                        "another@example.com"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You do not have access to this resume");

        verify(resumeAiService, never()).extractResumeData(anyString());
        verify(resumeRepository, never()).save(any(Resume.class));
    }

    @Test
    void runCareerSkillAgent_rejectsUserTryingToAccessAnotherUsersResume() {

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        assertThatThrownBy(() ->
                resumeService.runCareerSkillAgent(
                        1L,
                        "Java Full Stack Developer",
                        "another@example.com"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You do not have access to this resume");

        verify(careerSkillAgentService, never())
                .analyze(any(), anyString());
    }
}