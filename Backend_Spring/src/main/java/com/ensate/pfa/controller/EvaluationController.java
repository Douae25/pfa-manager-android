package com.ensate.pfa.controller;

import com.ensate.pfa.dto.response.EvaluationResponse;
import com.ensate.pfa.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    // Student use case: View evaluation by ID (for consult)
    @GetMapping("/{id}")
    public ResponseEntity<EvaluationResponse> getEvaluationById(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationService.getEvaluationById(id));
    }

    // Student use case: Consult evaluations (Consulter les évaluations)
    @GetMapping("/pfa/{pfaId}")
    public ResponseEntity<List<EvaluationResponse>> getEvaluationsByPfa(@PathVariable Long pfaId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByPfa(pfaId));
    }
}
