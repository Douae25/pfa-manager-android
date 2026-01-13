package com.ensate.pfa.dto.response;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDTO {
  private Long evaluationId;
  private Long pfaId;
  private Long evaluatorId;
  private Long dateEvaluation;
  private Double totalScore;

  // Détails des critères
  private List<EvaluationDetailDTO> details;
}