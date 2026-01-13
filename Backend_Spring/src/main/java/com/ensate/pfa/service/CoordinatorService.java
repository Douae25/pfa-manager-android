package com.ensate.pfa.service;

import com.ensate.pfa.dto.response.ProfessorAssignmentDTO;
import com.ensate.pfa.dto.response.ProfessorWithStatsDTO;
import com.ensate.pfa.dto.response.StudentWithEvaluationDTO;

import java.util.List;

public interface CoordinatorService {

    /**
     * Récupère tous les étudiants d'un département avec leurs évaluations
     * @param departmentId ID du département
     * @return Liste des étudiants avec leurs informations complètes
     */
    List<StudentWithEvaluationDTO> getStudentsWithEvaluations(Long departmentId);

    /**
     * Récupère tous les professeurs d'un département avec leurs statistiques
     * @param departmentId ID du département
     * @return Liste des professeurs avec nombre d'étudiants et moyenne
     */
    List<ProfessorWithStatsDTO> getProfessorsWithStats(Long departmentId);

    /**
     * Récupère toutes les affectations encadrant-étudiants d'un département
     * @param departmentId ID du département
     * @return Liste des professeurs avec leurs étudiants assignés
     */
    List<ProfessorAssignmentDTO> getAssignments(Long departmentId);

    /**
     * Affectation automatique et équitable des étudiants non affectés aux professeurs
     * @param departmentId ID du département
     * @return Nombre d'étudiants affectés
     */
    Integer autoAssignStudentsToProfessors(Long departmentId);
}