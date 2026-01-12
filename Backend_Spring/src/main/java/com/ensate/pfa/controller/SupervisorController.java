package com.ensate.pfa.controller;

import com.ensate.pfa.dto.response.*;
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
}