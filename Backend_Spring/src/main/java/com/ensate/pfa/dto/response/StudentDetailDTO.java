package com.ensate.pfa.dto.response;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetailDTO {
  // Infos étudiant
  private Long studentId;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;

  // PFA
  private Long pfaId;
  private String pfaTitle;
  private String pfaDescription;
  private String pfaStatus;
  private Long pfaUpdatedAt;

  // Convention
  private ConventionDTO convention;

  // Livrables
  private List<DeliverableDTO> deliverables;

  // Soutenance
  private SoutenanceDTO soutenance;

  // Évaluation
  private Double totalScore;
  private Boolean isEvaluated;
}