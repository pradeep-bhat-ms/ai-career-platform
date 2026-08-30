package com.pradeep.aicareerplatform.repository;

import com.pradeep.aicareerplatform.entity.LearningItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LearningItemRepository extends JpaRepository<LearningItem, Long> {
    List<LearningItem> findByLearningPlanIdOrderByWeekNumber(Long learningPlanId);
}