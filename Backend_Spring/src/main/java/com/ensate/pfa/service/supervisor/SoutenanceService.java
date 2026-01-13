package com.ensate.pfa.service.supervisor;

import com.ensate.pfa.dto.request.SoutenanceRequest;
import com.ensate.pfa.dto.response.*;
import com.ensate.pfa.entity.*;
import com.ensate.pfa.entity.enums.PFAStatus;
import com.ensate.pfa.entity.enums.SoutenanceStatus;
import com.ensate.pfa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SoutenanceService {

  private final SoutenanceRepository soutenanceRepository;
  private final PFADossierRepository pfaDossierRepository;
  private final EvaluationRepository evaluationRepository;

  /**
   * Récupère tous les PFAs avec leurs soutenances pour un superviseur
   */
  public List<PFAWithSoutenanceDTO> getPFAsWithSoutenances(Long supervisorId) {
    List<PFADossier> pfas = pfaDossierRepository.findBySupervisorUserId(supervisorId);

    return pfas.stream()
        .map(pfa -> {
          User student = pfa.getStudent();
          Soutenance soutenance = pfa.getSoutenance();

          // Chercher l'évaluation
          Evaluation evaluation = null;
          if (pfa.getEvaluations() != null && !pfa.getEvaluations().isEmpty()) {
            evaluation = pfa.getEvaluations().get(0);
          }

          return PFAWithSoutenanceDTO.builder()
              .pfaId(pfa.getPfaId())
              .title(pfa.getTitle())
              .description(pfa.getDescription())
              .status(pfa.getCurrentStatus() != null ? pfa.getCurrentStatus().name() : null)
              .studentId(student.getUserId())
              .studentFirstName(student.getFirstName())
              .studentLastName(student.getLastName())
              .soutenance(soutenance != null ? toSoutenanceDTO(soutenance, pfa, student) : null)
              .evaluation(evaluation != null ? toEvaluationDTO(evaluation) : null)
              .build();
        })
        .collect(Collectors.toList());
  }

  /**
   * Récupère toutes les soutenances d'un superviseur
   */
  public List<SoutenanceDTO> getSoutenancesBySupervisor(Long supervisorId) {
    List<PFADossier> pfas = pfaDossierRepository.findBySupervisorUserId(supervisorId);

    return pfas.stream()
        .filter(pfa -> pfa.getSoutenance() != null)
        .map(pfa -> toSoutenanceDTO(pfa.getSoutenance(), pfa, pfa.getStudent()))
        .collect(Collectors.toList());
  }

  /**
   * Planifier une nouvelle soutenance
   */
  @Transactional
  public SoutenanceDTO planifierSoutenance(Long supervisorId, SoutenanceRequest request) {
    // Vérifier que le PFA appartient au superviseur
    PFADossier pfa = pfaDossierRepository.findById(request.getPfaId())
        .orElseThrow(() -> new RuntimeException("PFA non trouvé"));

    if (!pfa.getSupervisor().getUserId().equals(supervisorId)) {
      throw new RuntimeException("Ce PFA n'est pas sous votre supervision");
    }

    // Vérifier qu'il n'y a pas déjà une soutenance
    if (pfa.getSoutenance() != null) {
      throw new RuntimeException("Une soutenance existe déjà pour ce PFA");
    }

    // Créer la soutenance
    Soutenance soutenance = Soutenance.builder()
        .pfaDossier(pfa)
        .location(request.getLocation())
        .dateSoutenance(request.getDateSoutenance())
        .status(SoutenanceStatus.PLANNED)
        .build();

    soutenance = soutenanceRepository.save(soutenance);

    // Mettre à jour le statut du PFA
    pfa.setCurrentStatus(PFAStatus.IN_PROGRESS);
    pfaDossierRepository.save(pfa);

    return toSoutenanceDTO(soutenance, pfa, pfa.getStudent());
  }

  /**
   * Modifier une soutenance existante
   */
  @Transactional
  public SoutenanceDTO modifierSoutenance(Long supervisorId, Long soutenanceId, SoutenanceRequest request) {
    Soutenance soutenance = soutenanceRepository.findById(soutenanceId)
        .orElseThrow(() -> new RuntimeException("Soutenance non trouvée"));

    PFADossier pfa = soutenance.getPfaDossier();

    if (!pfa.getSupervisor().getUserId().equals(supervisorId)) {
      throw new RuntimeException("Cette soutenance n'est pas sous votre supervision");
    }

    soutenance.setLocation(request.getLocation());
    soutenance.setDateSoutenance(request.getDateSoutenance());

    soutenance = soutenanceRepository.save(soutenance);

    return toSoutenanceDTO(soutenance, pfa, pfa.getStudent());
  }

  /**
   * Supprimer une soutenance
   */
  @Transactional
  public void supprimerSoutenance(Long supervisorId, Long soutenanceId) {
    Soutenance soutenance = soutenanceRepository.findById(soutenanceId)
        .orElseThrow(() -> new RuntimeException("Soutenance non trouvée"));

    PFADossier pfa = soutenance.getPfaDossier();

    if (!pfa.getSupervisor().getUserId().equals(supervisorId)) {
      throw new RuntimeException("Cette soutenance n'est pas sous votre supervision");
    }

    // Vider la liste des jurys (supprime les entrées dans
    // soutenance_jury)
    if (soutenance.getJuryMembers() != null) {
      soutenance.getJuryMembers().clear();
    }

    // Détacher du PFA
    pfa.setSoutenance(null);
    pfaDossierRepository.save(pfa);

    // Sauvegarder pour appliquer le clear()
    soutenanceRepository.save(soutenance);
    soutenanceRepository.flush();

    // supprimer la soutenance
    soutenanceRepository.delete(soutenance);
    soutenanceRepository.flush();
  }

  private SoutenanceDTO toSoutenanceDTO(Soutenance s, PFADossier pfa, User student) {
    return SoutenanceDTO.builder()
        .soutenanceId(s.getSoutenanceId())
        .pfaId(pfa.getPfaId())
        .location(s.getLocation())
        .dateSoutenance(s.getDateSoutenance())
        .status(s.getStatus() != null ? s.getStatus().name() : null)
        .createdAt(s.getCreatedAt())
        .pfaTitle(pfa.getTitle())
        .studentId(student.getUserId())
        .studentFirstName(student.getFirstName())
        .studentLastName(student.getLastName())
        .build();
  }

  private EvaluationDTO toEvaluationDTO(Evaluation e) {
    return EvaluationDTO.builder()
        .evaluationId(e.getEvaluationId())
        .pfaId(e.getPfaDossier().getPfaId())
        .evaluatorId(e.getEvaluator().getUserId())
        .dateEvaluation(e.getDateEvaluation())
        .totalScore(e.getTotalScore())
        .build();
  }
}