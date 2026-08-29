package com.pradeep.aicareerplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_answers")
@Getter
@Setter
@NoArgsConstructor
public class InterviewAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "question_id", nullable = false)
    private InterviewQuestion question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String answerText;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();
}