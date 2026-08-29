
package com.pradeep.aicareerplatform.repository;

import com.pradeep.aicareerplatform.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    List<InterviewQuestion> findBySessionIdOrderByQuestionOrder(Long sessionId);
}