// dto/response/PFAWithSoutenanceDTO.java
package com.ensate.pfa.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PFAWithSoutenanceDTO {
  // Infos PFA
  private Long pfaId;
  private String title;
  private String description;
  private String status;

  // Infos étudiant
  private Long studentId;
  private String studentFirstName;
  private String studentLastName;

  // Soutenance (peut être null)
  private SoutenanceDTO soutenance;

  // Évaluation (peut être null)
  private EvaluationDTO evaluation;

  public boolean isPlanned() {
    return soutenance != null;
  }

  public boolean isEvaluated() {
    return evaluation != null && evaluation.getTotalScore() != null;
  }
}