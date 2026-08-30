package com.pradeep.aicareerplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_plans")
@Getter
@Setter
@NoArgsConstructor
public class LearningPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String targetRole;

    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();
}