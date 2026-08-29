package com.pradeep.aicareerplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interview_evaluations")
@Getter
@Setter
@NoArgsConstructor
public class InterviewEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "answer_id", nullable = false)
    private InterviewAnswer answer;

    @Column(nullable = false)
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;
}