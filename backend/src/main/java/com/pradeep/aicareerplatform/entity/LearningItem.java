package com.pradeep.aicareerplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "learning_items")
@Getter
@Setter
@NoArgsConstructor
public class LearningItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "learning_plan_id", nullable = false)
    private LearningPlan learningPlan;

    @Column(nullable = false)
    private Integer weekNumber;

    @Column(nullable = false)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String resourceSuggestion;

    @Column(nullable = false)
    private boolean completed = false;
}