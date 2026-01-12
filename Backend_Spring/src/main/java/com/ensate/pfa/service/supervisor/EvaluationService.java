package com.ensate.pfa.service.supervisor;

import com.ensate.pfa.dto.request.EvaluationRequest;
import com.ensate.pfa.dto.response.*;
import com.ensate.pfa.entity.*;
import com.ensate.pfa.entity.enums.PFAStatus;
import com.ensate.pfa.entity.enums.SoutenanceStatus;
import com.ensate.pfa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationService {

  private final EvaluationRepository evaluationRepository;
  private final EvaluationCriteriaRepository criteriaRepository;
  private final EvaluationDetailRepository detailRepository;
  private final PFADossierRepository pfaDossierRepository;
  private final SoutenanceRepository soutenanceRepository;
  private final UserRepository userRepository;

  /**
   * Récupère les critères d'évaluation actifs
   */
  public List<EvaluationCriteriaDTO> getActiveCriteria() {
    List<EvaluationCriteria> criteriaList = criteriaRepository.findAllActive();

    return criteriaList.stream()
        .map(this::toEvaluationCriteriaDTO)
        .collect(Collectors.toList());
  }

  private EvaluationCriteriaDTO toEvaluationCriteriaDTO(EvaluationCriteria c) {
    return EvaluationCriteriaDTO.builder()
        .criteriaId(c.getCriteriaId())
        .label(c.getLabel())
        .weight(c.getMaxPoints() != null ? c.getMaxPoints().doubleValue() : 0.0)
        .description(c.getDescription())
        .isActive(c.getIsActive())
        .build();
  }

  /**
   * Récupère les soutenances avec évaluations pour un superviseur
   */
  public List<SoutenanceWithEvaluationDTO> getSoutenancesWithEvaluations(Long supervisorId) {
    List<PFADossier> pfas = pfaDossierRepository.findBySupervisorUserId(supervisorId);

    List<SoutenanceWithEvaluationDTO> result = new ArrayList<>();

    for (PFADossier pfa : pfas) {
      Soutenance soutenance = pfa.getSoutenance();
      if (soutenance == null)
        continue;

      User student = pfa.getStudent();
      Evaluation evaluation = (pfa.getEvaluations() != null && !pfa.getEvaluations().isEmpty())
          ? pfa.getEvaluations().get(0)
          : null;

      result.add(SoutenanceWithEvaluationDTO.builder()
          .soutenanceId(soutenance.getSoutenanceId())
          .pfaId(pfa.getPfaId())
          .pfaTitle(pfa.getTitle())
          .studentId(student.getUserId())
          .studentFirstName(student.getFirstName())
          .studentLastName(student.getLastName())
          .dateSoutenance(soutenance.getDateSoutenance())
          .location(soutenance.getLocation())
          .soutenanceStatus(soutenance.getStatus() != null ? soutenance.getStatus().name() : null)
          .totalScore(evaluation != null ? evaluation.getTotalScore() : null)
          .isEvaluated(evaluation != null && evaluation.getTotalScore() != null)
          .build());
    }

    return result;
  }

  /**
   * Enregistrer une évaluation
   */
  @Transactional
  public EvaluationDTO saveEvaluation(Long supervisorId, EvaluationRequest request) {
    PFADossier pfa = pfaDossierRepository.findById(request.getPfaId())
        .orElseThrow(() -> new RuntimeException("PFA non trouvé"));

    if (!pfa.getSupervisor().getUserId().equals(supervisorId)) {
      throw new RuntimeException("Ce PFA n'est pas sous votre supervision");
    }

    if (evaluationRepository.existsByPfaDossierPfaId(request.getPfaId())) {
      throw new RuntimeException("Ce projet a déjà été évalué");
    }

    User evaluator = userRepository.findById(supervisorId)
        .orElseThrow(() -> new RuntimeException("Évaluateur non trouvé"));

    // Calculer le score total
    double totalScore = 0.0;
    double totalWeight = 0.0;

    for (EvaluationRequest.CriteriaScoreRequest score : request.getScores()) {
      EvaluationCriteria criteria = criteriaRepository.findById(score.getCriteriaId())
          .orElseThrow(() -> new RuntimeException("Critère non trouvé: " + score.getCriteriaId()));

      double weight = criteria.getMaxPoints() != null ? criteria.getMaxPoints().doubleValue() : 0.0;
      totalScore += score.getScore() * weight;
      totalWeight += weight;
    }

    if (totalWeight > 0) {
      totalScore = totalScore / totalWeight;
    }

    // Créer l'évaluation
    Evaluation evaluation = Evaluation.builder()
        .pfaDossier(pfa)
        .evaluator(evaluator)
        .dateEvaluation(System.currentTimeMillis())
        .totalScore(totalScore)
        .build();

    evaluation = evaluationRepository.save(evaluation);

    // Créer les détails
    List<EvaluationDetailDTO> detailDTOs = new ArrayList<>();
    for (EvaluationRequest.CriteriaScoreRequest score : request.getScores()) {
      EvaluationCriteria criteria = criteriaRepository.findById(score.getCriteriaId())
          .orElseThrow(() -> new RuntimeException("Critère non trouvé"));

      EvaluationDetail detail = EvaluationDetail.builder()
          .evaluation(evaluation)
          .criteria(criteria)
          .scoreGiven(score.getScore())
          .build();

      detailRepository.save(detail);

      // ═══════════════════════════════════════════════════════════
      // CORRECTION ICI : Convertir Integer en Double
      // ═══════════════════════════════════════════════════════════
      detailDTOs.add(EvaluationDetailDTO.builder()
          .detailId(detail.getDetailId())
          .criteriaId(criteria.getCriteriaId())
          .criteriaLabel(criteria.getLabel())
          .criteriaWeight(criteria.getMaxPoints() != null ? criteria.getMaxPoints().doubleValue() : 0.0)
          .scoreGiven(score.getScore())
          .build());
    }

    // Mettre à jour le statut de la soutenance
    Soutenance soutenance = pfa.getSoutenance();
    if (soutenance != null) {
      soutenance.setStatus(SoutenanceStatus.DONE);
      soutenanceRepository.save(soutenance);
    }

    // Mettre à jour le statut du PFA
    pfa.setCurrentStatus(PFAStatus.CLOSED);
    pfaDossierRepository.save(pfa);

    return EvaluationDTO.builder()
        .evaluationId(evaluation.getEvaluationId())
        .pfaId(pfa.getPfaId())
        .evaluatorId(supervisorId)
        .dateEvaluation(evaluation.getDateEvaluation())
        .totalScore(totalScore)
        .details(detailDTOs)
        .build();
  }
}