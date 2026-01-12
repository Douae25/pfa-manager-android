package com.ensate.pfa.controller;

import com.ensate.pfa.dto.request.EvaluationRequest;
import com.ensate.pfa.dto.request.SoutenanceRequest;
import com.ensate.pfa.dto.response.*;
import com.ensate.pfa.service.EvaluationService;
import com.ensate.pfa.service.SoutenanceService;
import com.ensate.pfa.service.SupervisorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supervisor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SupervisorController {

  private final SupervisorService supervisorService;
  private final SoutenanceService soutenanceService;
    private final EvaluationService evaluationService;

  /**
   * GET /api/supervisor/students?supervisorId=10
   * Liste des étudiants avec leurs PFA
   */
  @GetMapping("/students")
  public ResponseEntity<ApiResponse<List<StudentWithPFADTO>>> getMyStudents(
      @RequestParam Long supervisorId) {
    try {
      List<StudentWithPFADTO> students = supervisorService.getMyStudents(supervisorId);
      return ResponseEntity.ok(ApiResponse.success(students));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  /**
   * GET /api/supervisor/students/50?supervisorId=10
   * Détail complet d'un étudiant (PFA, Convention, Livrables, Soutenance)
   */
  @GetMapping("/students/{studentId}")
  public ResponseEntity<ApiResponse<StudentDetailDTO>> getStudentDetail(
      @RequestParam Long supervisorId,
      @PathVariable Long studentId) {
    try {
      StudentDetailDTO detail = supervisorService.getStudentDetail(supervisorId, studentId);
      return ResponseEntity.ok(ApiResponse.success(detail));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  /**
   * GET /api/supervisor/deliverables?supervisorId=10
   * Tous les livrables des étudiants du superviseur
   */
  @GetMapping("/deliverables")
  public ResponseEntity<ApiResponse<List<DeliverableDTO>>> getAllDeliverables(
      @RequestParam Long supervisorId) {
    try {
      List<DeliverableDTO> deliverables = supervisorService.getAllDeliverables(supervisorId);
      return ResponseEntity.ok(ApiResponse.success(deliverables));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  /**
   * GET /api/supervisor/deliverables/pfa/20
   * Livrables d'un PFA spécifique
   */
  @GetMapping("/deliverables/pfa/{pfaId}")
  public ResponseEntity<ApiResponse<List<DeliverableDTO>>> getDeliverablesByPfa(
      @PathVariable Long pfaId) {
    try {
      List<DeliverableDTO> deliverables = supervisorService.getDeliverablesByPfa(pfaId);
      return ResponseEntity.ok(ApiResponse.success(deliverables));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }


  /**
     * GET /api/supervisor/soutenances/pfas?supervisorId=10
     * Liste des PFAs avec leurs soutenances
     */
    @GetMapping("/soutenances/pfas")
    public ResponseEntity<ApiResponse<List<PFAWithSoutenanceDTO>>> getPFAsWithSoutenances(
            @RequestParam Long supervisorId) {
        try {
            List<PFAWithSoutenanceDTO> pfas = soutenanceService.getPFAsWithSoutenances(supervisorId);
            return ResponseEntity.ok(ApiResponse.success(pfas));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/supervisor/soutenances?supervisorId=10
     * Liste des soutenances uniquement
     */
    @GetMapping("/soutenances")
    public ResponseEntity<ApiResponse<List<SoutenanceDTO>>> getSoutenances(
            @RequestParam Long supervisorId) {
        try {
            List<SoutenanceDTO> soutenances = soutenanceService.getSoutenancesBySupervisor(supervisorId);
            return ResponseEntity.ok(ApiResponse.success(soutenances));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/supervisor/soutenances?supervisorId=10
     * Planifier une nouvelle soutenance
     */
    @PostMapping("/soutenances")
    public ResponseEntity<ApiResponse<SoutenanceDTO>> planifierSoutenance(
            @RequestParam Long supervisorId,
            @RequestBody SoutenanceRequest request) {
        try {
            SoutenanceDTO soutenance = soutenanceService.planifierSoutenance(supervisorId, request);
            return ResponseEntity.ok(ApiResponse.success(soutenance));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/supervisor/soutenances/{id}?supervisorId=10
     * Modifier une soutenance
     */
    @PutMapping("/soutenances/{soutenanceId}")
    public ResponseEntity<ApiResponse<SoutenanceDTO>> modifierSoutenance(
            @RequestParam Long supervisorId,
            @PathVariable Long soutenanceId,
            @RequestBody SoutenanceRequest request) {
        try {
            SoutenanceDTO soutenance = soutenanceService.modifierSoutenance(supervisorId, soutenanceId, request);
            return ResponseEntity.ok(ApiResponse.success(soutenance));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /api/supervisor/soutenances/{id}?supervisorId=10
     * Supprimer une soutenance
     */
    @DeleteMapping("/soutenances/{soutenanceId}")
    public ResponseEntity<ApiResponse<Void>> supprimerSoutenance(
            @RequestParam Long supervisorId,
            @PathVariable Long soutenanceId) {
        try {
            soutenanceService.supprimerSoutenance(supervisorId, soutenanceId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }



    /**
     * GET /api/supervisor/evaluation-criteria
     * Récupérer les critères d'évaluation actifs
     */
    @GetMapping("/evaluation-criteria")
    public ResponseEntity<ApiResponse<List<EvaluationCriteriaDTO>>> getEvaluationCriteria() {
        try {
            List<EvaluationCriteriaDTO> criteria = evaluationService.getActiveCriteria();
            return ResponseEntity.ok(ApiResponse.success(criteria));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/supervisor/evaluations?supervisorId=10
     * Récupérer les soutenances avec leur statut d'évaluation
     */
    @GetMapping("/evaluations")
    public ResponseEntity<ApiResponse<List<SoutenanceWithEvaluationDTO>>> getSoutenancesWithEvaluations(
            @RequestParam Long supervisorId) {
        try {
            List<SoutenanceWithEvaluationDTO> items = evaluationService.getSoutenancesWithEvaluations(supervisorId);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/supervisor/evaluations?supervisorId=10
     * Enregistrer une évaluation
     */
    @PostMapping("/evaluations")
    public ResponseEntity<ApiResponse<EvaluationDTO>> saveEvaluation(
            @RequestParam Long supervisorId,
            @RequestBody EvaluationRequest request) {
        try {
            EvaluationDTO evaluation = evaluationService.saveEvaluation(supervisorId, request);
            return ResponseEntity.ok(ApiResponse.success(evaluation));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}