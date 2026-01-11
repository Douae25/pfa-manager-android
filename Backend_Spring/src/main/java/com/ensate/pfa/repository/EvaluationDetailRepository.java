package com.ensate.pfa.repository;

import com.ensate.pfa.entity.EvaluationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationDetailRepository extends JpaRepository<EvaluationDetail, Long> {
    List<EvaluationDetail> findByEvaluationEvaluationId(Long evaluationId);
}
