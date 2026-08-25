package com.pradeep.aicareerplatform.repository;

import com.pradeep.aicareerplatform.entity.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
    List<JobDescription> findByUserId(Long userId);
}