package com.ensate.pfa.controller;

import com.ensate.pfa.dto.request.AutoAssignRequest;
import com.ensate.pfa.dto.response.*;
import com.ensate.pfa.service.CoordinatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coordinator")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CoordinatorController {

    private final CoordinatorService coordinatorService;

    /**
     * Liste des étudiants avec évaluations
     * GET /api/coordinator/students/{departmentId}
     */
    @GetMapping("/students/{departmentId}")
    public ResponseEntity<ApiResponse<List<StudentWithEvaluationDTO>>> getStudentsWithEvaluations(
            @PathVariable Long departmentId) {
        log.info("GET /api/coordinator/students/{}", departmentId);

        List<StudentWithEvaluationDTO> students = coordinatorService.getStudentsWithEvaluations(departmentId);

        return ResponseEntity.ok(
                ApiResponse.<List<StudentWithEvaluationDTO>>builder()
                        .success(true)
                        .message("Liste des étudiants récupérée avec succès")
                        .data(students)
                        .build()
        );
    }

    /**
     * Liste des professeurs avec statistiques
     * GET /api/coordinator/professors/{departmentId}
     */
    @GetMapping("/professors/{departmentId}")
    public ResponseEntity<ApiResponse<List<ProfessorWithStatsDTO>>> getProfessorsWithStats(
            @PathVariable Long departmentId) {
        log.info("GET /api/coordinator/professors/{}", departmentId);

        List<ProfessorWithStatsDTO> professors = coordinatorService.getProfessorsWithStats(departmentId);

        return ResponseEntity.ok(
                ApiResponse.<List<ProfessorWithStatsDTO>>builder()
                        .success(true)
                        .message("Liste des professeurs récupérée avec succès")
                        .data(professors)
                        .build()
        );
    }

    /**
     * Affectations encadrants-étudiants
     * GET /api/coordinator/assignments/{departmentId}
     */
    @GetMapping("/assignments/{departmentId}")
    public ResponseEntity<ApiResponse<List<ProfessorAssignmentDTO>>> getAssignments(
            @PathVariable Long departmentId) {
        log.info("GET /api/coordinator/assignments/{}", departmentId);

        List<ProfessorAssignmentDTO> assignments = coordinatorService.getAssignments(departmentId);

        return ResponseEntity.ok(
                ApiResponse.<List<ProfessorAssignmentDTO>>builder()
                        .success(true)
                        .message("Affectations récupérées avec succès")
                        .data(assignments)
                        .build()
        );
    }

    /**
     * Affectation automatique équitable
     * POST /api/coordinator/assignments/auto-assign
     */
    @PostMapping("/assignments/auto-assign")
    public ResponseEntity<ApiResponse<Integer>> autoAssignStudents(
            @RequestBody AutoAssignRequest request) {
        log.info("POST /api/coordinator/assignments/auto-assign pour département {}", request.getDepartmentId());

        Integer assignedCount = coordinatorService.autoAssignStudentsToProfessors(request.getDepartmentId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<Integer>builder()
                        .success(true)
                        .message(assignedCount + " étudiant(s) affecté(s) avec succès")
                        .data(assignedCount)
                        .build()
        );
    }
}