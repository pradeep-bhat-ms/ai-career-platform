package com.pradeep.aicareerplatform.repository;

import com.pradeep.aicareerplatform.entity.CodeChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodeChallengeRepository extends JpaRepository<CodeChallenge, Long> {
    List<CodeChallenge> findByCategory(String category);
    List<CodeChallenge> findByDifficulty(String difficulty);
}