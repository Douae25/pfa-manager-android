package com.ensate.pfa.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationCriteriaDTO {
  private Long criteriaId;
  private String label;
  private Double weight;
  private String description;
  private Boolean isActive;
}