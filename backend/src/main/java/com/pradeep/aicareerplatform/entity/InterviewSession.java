package com.pradeep.aicareerplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_sessions")
@Getter
@Setter
@NoArgsConstructor
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String interviewType; // Technical, HR, Project, JD-based

    @Column(nullable = false)
    private String difficulty; // Easy, Medium, Hard

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private String status = "IN_PROGRESS"; // IN_PROGRESS, COMPLETED

    private Integer finalScore;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;
}