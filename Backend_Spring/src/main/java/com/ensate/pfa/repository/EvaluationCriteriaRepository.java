package com.ensate.pfa.repository;

import com.ensate.pfa.entity.EvaluationCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, Long> {
    List<EvaluationCriteria> findByIsActiveTrue();

    @Query("SELECT c FROM EvaluationCriteria c WHERE c.isActive = true")
    List<EvaluationCriteria> findAllActive();
}
