package com.pradeep.aicareerplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "skill_gaps")
@Getter
@Setter
@NoArgsConstructor
public class SkillGap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String skillName;

    @Column(nullable = false)
    private String status; // Strong, Medium, Weak, Missing

    @Column(nullable = false)
    private String targetRole;

    @Column(nullable = false, updatable = false)
    private LocalDateTime computedAt = LocalDateTime.now();
}