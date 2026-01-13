package com.ensate.pfa.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvaluationResponse {
    private Long evaluationId;
    private Long pfaId;
    private String pfaTitle;
    private Long evaluatorId;
    private String evaluatorName;
    private Long dateEvaluation;
    private Double totalScore;
}
