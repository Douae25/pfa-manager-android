// dto/request/EvaluationRequest.java
package com.ensate.pfa.dto.request;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRequest {
  private Long pfaId;
  private List<CriteriaScoreRequest> scores;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CriteriaScoreRequest {
    private Long criteriaId;
    private Double score;
  }
}