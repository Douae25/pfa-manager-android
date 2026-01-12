package com.ensate.pfa.repository;

import com.ensate.pfa.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByPfaDossierPfaId(Long pfaId);

    List<Evaluation> findByEvaluatorUserId(Long evaluatorId);

    Optional<Evaluation> findFirstByPfaDossierPfaId(Long pfaId);

    @Query("SELECT AVG(e.totalScore) FROM Evaluation e WHERE e.pfaDossier.pfaId = :pfaId")
    Double calculateAverageScoreByPfaId(Long pfaId);
}
