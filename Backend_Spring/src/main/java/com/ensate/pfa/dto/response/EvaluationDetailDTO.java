// dto/response/EvaluationDetailDTO.java
package com.ensate.pfa.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDetailDTO {
  private Long detailId;
  private Long criteriaId;
  private String criteriaLabel;
  private Double criteriaWeight;
  private Double scoreGiven;
}