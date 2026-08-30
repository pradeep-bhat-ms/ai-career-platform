
package com.pradeep.aicareerplatform.repository;

import com.pradeep.aicareerplatform.entity.SkillGap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SkillGapRepository extends JpaRepository<SkillGap, Long> {
    List<SkillGap> findByUserIdAndTargetRole(Long userId, String targetRole);
}