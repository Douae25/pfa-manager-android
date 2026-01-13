package com.ensate.pfa.service.impl;

import com.ensate.pfa.dto.response.EvaluationResponse;
import com.ensate.pfa.entity.Evaluation;
import com.ensate.pfa.exception.ResourceNotFoundException;
import com.ensate.pfa.repository.EvaluationRepository;
import com.ensate.pfa.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationResponse> getEvaluationsByPfa(Long pfaId) {
        return evaluationRepository.findByPfaDossierPfaId(pfaId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationResponse getEvaluationById(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation", id));
        return mapToResponse(evaluation);
    }

    private EvaluationResponse mapToResponse(Evaluation evaluation) {
        return EvaluationResponse.builder()
                .evaluationId(evaluation.getEvaluationId())
                .pfaId(evaluation.getPfaDossier().getPfaId())
                .pfaTitle(evaluation.getPfaDossier().getTitle())
                .evaluatorId(evaluation.getEvaluator().getUserId())
                .evaluatorName(evaluation.getEvaluator().getFirstName() + " " + evaluation.getEvaluator().getLastName())
                .dateEvaluation(evaluation.getDateEvaluation())
                .totalScore(evaluation.getTotalScore())
                .build();
    }
}
