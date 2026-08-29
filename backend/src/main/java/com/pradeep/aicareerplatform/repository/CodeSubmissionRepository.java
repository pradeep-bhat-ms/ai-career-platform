package com.pradeep.aicareerplatform.repository;

import com.pradeep.aicareerplatform.entity.CodeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodeSubmissionRepository extends JpaRepository<CodeSubmission, Long> {
    List<CodeSubmission> findByUserIdOrderBySubmittedAtDesc(Long userId);
    List<CodeSubmission> findByUserIdAndChallengeId(Long userId, Long challengeId);
}