
package com.pradeep.aicareerplatform.repository;

import com.pradeep.aicareerplatform.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByUserId(Long userId);
}