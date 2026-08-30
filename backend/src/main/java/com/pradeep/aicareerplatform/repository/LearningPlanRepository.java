
package com.pradeep.aicareerplatform.repository;

import com.pradeep.aicareerplatform.entity.LearningPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LearningPlanRepository extends JpaRepository<LearningPlan, Long> {
    List<LearningPlan> findByUserId(Long userId);
    Optional<LearningPlan> findTopByUserIdAndTargetRoleOrderByGeneratedAtDesc(Long userId, String targetRole);
}