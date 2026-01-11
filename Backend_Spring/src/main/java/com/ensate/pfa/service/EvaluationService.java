package com.ensate.pfa.service;

import com.ensate.pfa.dto.response.EvaluationResponse;

import java.util.List;

public interface EvaluationService {
    // Student use case: Consult evaluations
    List<EvaluationResponse> getEvaluationsByPfa(Long pfaId);
    EvaluationResponse getEvaluationById(Long id);
}
