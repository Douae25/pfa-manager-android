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
   * GET /api/supervisor/students?supervisorId=1
   * Récupère la liste des étudiants de l'encadrant
   */
  @GetMapping("/students")
  public ResponseEntity<ApiResponse<List<StudentWithPFADTO>>> getMyStudents(
      @RequestParam Long supervisorId) {

    try {
      List<StudentWithPFADTO> students = supervisorService.getMyStudents(supervisorId);
      return ResponseEntity.ok(ApiResponse.success(students));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Erreur: " + e.getMessage()));
    }
  }

  /**
   * GET /api/supervisor/students/5?supervisorId=1
   * Récupère le détail d'un étudiant
   */
  @GetMapping("/students/{studentId}")
  public ResponseEntity<ApiResponse<StudentDetailDTO>> getStudentDetail(
      @RequestParam Long supervisorId,
      @PathVariable Long studentId) {

    try {
      StudentDetailDTO student = supervisorService.getStudentDetail(supervisorId, studentId);
      return ResponseEntity.ok(ApiResponse.success(student));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Erreur: " + e.getMessage()));
    }
  }
}