// service/SupervisorService.java
package com.ensate.pfa.service;

import com.ensate.pfa.dto.response.*;
import com.ensate.pfa.entity.*;
import com.ensate.pfa.entity.enums.PFAStatus;
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
  private final EvaluationRepository evaluationRepository;
  private final ConventionRepository conventionRepository;
  private final DeliverableRepository deliverableRepository;
  private final SoutenanceRepository soutenanceRepository;

  /**
   * Récupère tous les étudiants d'un encadrant avec leurs PFA
   */
  public List<StudentWithPFADTO> getMyStudents(Long supervisorId) {
    List<PFADossier> pfaDossiers = pfaDossierRepository.findBySupervisorUserId(supervisorId);

    return pfaDossiers.stream()
        .map(pfa -> {
          User student = pfa.getStudent();

          // Chercher l'évaluation
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
   * Récupère le détail d'un étudiant spécifique
   */
  public StudentDetailDTO getStudentDetail(Long supervisorId, Long studentId) {
    List<PFADossier> pfas = pfaDossierRepository.findBySupervisorUserId(supervisorId);

    PFADossier pfa = pfas.stream()
        .filter(p -> p.getStudent().getUserId().equals(studentId))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

    User student = pfa.getStudent();

    // Construire le DTO
    return StudentDetailDTO.builder()
        .studentId(student.getUserId())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .email(student.getEmail())
        .phoneNumber(student.getPhoneNumber())
        .pfa(buildPFAInfo(pfa))
        .convention(buildConvention(pfa.getConvention()))
        .deliverables(buildDeliverables(pfa.getDeliverables()))
        .soutenance(buildSoutenance(pfa.getSoutenance()))
        .evaluation(buildEvaluation(pfa.getEvaluations()))
        .build();
  }

  private PFAInfoDTO buildPFAInfo(PFADossier pfa) {
    return PFAInfoDTO.builder()
        .pfaId(pfa.getPfaId())
        .title(pfa.getTitle())
        .description(pfa.getDescription())
        .currentStatus(pfa.getCurrentStatus())
        .updatedAt(pfa.getUpdatedAt())
        .build();
  }

  private ConventionDTO buildConvention(Convention convention) {
    if (convention == null)
      return null;
    return ConventionDTO.builder()
        .conventionId(convention.getConventionId())
        .companyName(convention.getCompanyName())
        .companyAddress(convention.getCompanyAddress())
        .state(convention.getState())
        .scannedFileUri(convention.getScannedFileUri())
        .build();
  }

  private List<DeliverableDTO> buildDeliverables(List<Deliverable> deliverables) {
    if (deliverables == null)
      return List.of();
    return deliverables.stream()
        .map(d -> DeliverableDTO.builder()
            .deliverableId(d.getDeliverableId())
            .fileTitle(d.getFileTitle())
            .fileUri(d.getFileUri())
            .uploadedAt(d.getUploadedAt())
            .deliverableType(d.getDeliverableType())
            .build())
        .collect(Collectors.toList());
  }

  private SoutenanceDTO buildSoutenance(Soutenance soutenance) {
    if (soutenance == null)
      return null;
    return SoutenanceDTO.builder()
        .soutenanceId(soutenance.getSoutenanceId())
        .dateSoutenance(soutenance.getDateSoutenance())
        .location(soutenance.getLocation())
        .status(soutenance.getStatus())
        .build();
  }

  private EvaluationDTO buildEvaluation(List<Evaluation> evaluations) {
    if (evaluations == null || evaluations.isEmpty())
      return null;
    Evaluation eval = evaluations.get(0);
    return EvaluationDTO.builder()
        .evaluationId(eval.getEvaluationId())
        .totalScore(eval.getTotalScore())
        // .comments(eval.getComments())
        .build();
  }
}