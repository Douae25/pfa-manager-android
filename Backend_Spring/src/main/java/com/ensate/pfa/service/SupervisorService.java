package com.ensate.pfa.service;

import com.ensate.pfa.dto.response.*;
import com.ensate.pfa.entity.*;
import com.ensate.pfa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupervisorService {

  private final PFADossierRepository pfaDossierRepository;
  private final DeliverableRepository deliverableRepository;
  private final ConventionRepository conventionRepository;
  private final SoutenanceRepository soutenanceRepository;
  private final EvaluationRepository evaluationRepository;

  /**
   * Récupère tous les étudiants d'un encadrant avec leurs PFA
   */
  public List<StudentWithPFADTO> getMyStudents(Long supervisorId) {
    List<PFADossier> pfaDossiers = pfaDossierRepository.findBySupervisorUserId(supervisorId);

    return pfaDossiers.stream()
        .map(pfa -> {
          User student = pfa.getStudent();

          Double score = null;
          boolean evaluated = false;

          List<Evaluation> evaluations = pfa.getEvaluations();
          if (evaluations != null && !evaluations.isEmpty()) {
            Evaluation eval = evaluations.get(0);
            if (eval.getTotalScore() != null) {
              score = eval.getTotalScore();
              evaluated = true;
            }
          }

          return StudentWithPFADTO.builder()
              .studentId(student.getUserId())
              .firstName(student.getFirstName())
              .lastName(student.getLastName())
              .email(student.getEmail())
              .phoneNumber(student.getPhoneNumber())
              .pfaId(pfa.getPfaId())
              .pfaTitle(pfa.getTitle())
              .pfaDescription(pfa.getDescription())
              .pfaStatus(pfa.getCurrentStatus())
              .totalScore(score)
              .evaluated(evaluated)
              .build();
        })
        .collect(Collectors.toList());
  }

  /**
   * Récupère le détail complet d'un étudiant (PFA, Convention, Livrables,
   * Soutenance)
   */
  public StudentDetailDTO getStudentDetail(Long supervisorId, Long studentId) {
    PFADossier pfa = pfaDossierRepository.findBySupervisorUserId(supervisorId)
        .stream()
        .filter(p -> p.getStudent().getUserId().equals(studentId))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

    User student = pfa.getStudent();

    // Convention
    ConventionDTO conventionDTO = null;
    if (pfa.getConvention() != null) {
      conventionDTO = toConventionDTO(pfa.getConvention());
    }

    // Livrables
    List<DeliverableDTO> deliverableDTOs = pfa.getDeliverables() != null ? pfa.getDeliverables().stream()
        .map(d -> toDeliverableDTO(d, student, pfa))
        .collect(Collectors.toList()) : List.of();

    // Soutenance
    SoutenanceDTO soutenanceDTO = null;
    if (pfa.getSoutenance() != null) {
      soutenanceDTO = toSoutenanceDTO(pfa.getSoutenance());
    }

    // Évaluation
    Double totalScore = null;
    boolean isEvaluated = false;
    if (pfa.getEvaluations() != null && !pfa.getEvaluations().isEmpty()) {
      Evaluation eval = pfa.getEvaluations().get(0);
      if (eval.getTotalScore() != null) {
        totalScore = eval.getTotalScore();
        isEvaluated = true;
      }
    }

    return StudentDetailDTO.builder()
        .studentId(student.getUserId())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .email(student.getEmail())
        .phoneNumber(student.getPhoneNumber())
        .pfaId(pfa.getPfaId())
        .pfaTitle(pfa.getTitle())
        .pfaDescription(pfa.getDescription())
        .pfaStatus(pfa.getCurrentStatus() != null ? pfa.getCurrentStatus().name() : null)
        .pfaUpdatedAt(pfa.getUpdatedAt())
        .convention(conventionDTO)
        .deliverables(deliverableDTOs)
        .soutenance(soutenanceDTO)
        .totalScore(totalScore)
        .isEvaluated(isEvaluated)
        .build();
  }

  /**
   * Récupère tous les livrables des étudiants d'un superviseur
   */
  public List<DeliverableDTO> getAllDeliverables(Long supervisorId) {
    List<Deliverable> deliverables = deliverableRepository.findBySupervisorId(supervisorId);

    return deliverables.stream()
        .map(d -> {
          PFADossier pfa = d.getPfaDossier();
          User student = pfa.getStudent();
          return toDeliverableDTO(d, student, pfa);
        })
        .collect(Collectors.toList());
  }

  /**
   * Récupère les livrables d'un PFA spécifique
   */
  public List<DeliverableDTO> getDeliverablesByPfa(Long pfaId) {
    List<Deliverable> deliverables = deliverableRepository.findByPfaDossierPfaId(pfaId);

    return deliverables.stream()
        .map(d -> {
          PFADossier pfa = d.getPfaDossier();
          User student = pfa.getStudent();
          return toDeliverableDTO(d, student, pfa);
        })
        .collect(Collectors.toList());
  }

  // ═══════════════════════════════════════════════════════════════════
  // MAPPERS
  // ═══════════════════════════════════════════════════════════════════

  private ConventionDTO toConventionDTO(Convention c) {
    return ConventionDTO.builder()
        .conventionId(c.getConventionId())
        .pfaId(c.getPfaDossier().getPfaId())
        .companyName(c.getCompanyName())
        .companyAddress(c.getCompanyAddress())
        .companySupervisorName(c.getCompanySupervisorName())
        .companySupervisorEmail(c.getCompanySupervisorEmail())
        .startDate(c.getStartDate())
        .endDate(c.getEndDate())
        .scannedFileUri(c.getScannedFileUri())
        .isValidated(c.getIsValidated())
        .state(c.getState() != null ? c.getState().name() : null)
        .adminComment(c.getAdminComment())
        .build();
  }

  private DeliverableDTO toDeliverableDTO(Deliverable d, User student, PFADossier pfa) {
    return DeliverableDTO.builder()
        .deliverableId(d.getDeliverableId())
        .pfaId(pfa.getPfaId())
        .fileTitle(d.getFileTitle())
        .fileUri(d.getFileUri())
        .deliverableType(d.getDeliverableType() != null ? d.getDeliverableType().name() : null)
        .deliverableFileType(d.getFileType() != null ? d.getFileType().name() : null)
        .uploadedAt(d.getUploadedAt())
        .isValidated(d.getIsValidated())
        .studentId(student.getUserId())
        .studentFirstName(student.getFirstName())
        .studentLastName(student.getLastName())
        .pfaTitle(pfa.getTitle())
        .build();
  }

  private SoutenanceDTO toSoutenanceDTO(Soutenance s) {
    return SoutenanceDTO.builder()
        .soutenanceId(s.getSoutenanceId())
        .pfaId(s.getPfaDossier().getPfaId())
        .location(s.getLocation())
        .dateSoutenance(s.getDateSoutenance())
        .status(s.getStatus() != null ? s.getStatus().name() : null)
        .createdAt(s.getCreatedAt())
        .build();
  }
}