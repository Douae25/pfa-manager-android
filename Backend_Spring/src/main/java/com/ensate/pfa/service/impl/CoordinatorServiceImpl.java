package com.ensate.pfa.service.impl;

import com.ensate.pfa.dto.response.*;
import com.ensate.pfa.entity.*;
import com.ensate.pfa.entity.enums.PFAStatus;
import com.ensate.pfa.entity.enums.Role;
import com.ensate.pfa.exception.BadRequestException;
import com.ensate.pfa.exception.ResourceNotFoundException;
import com.ensate.pfa.repository.*;
import com.ensate.pfa.service.CoordinatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoordinatorServiceImpl implements CoordinatorService {

    private final UserRepository userRepository;
    private final PFADossierRepository pfaDossierRepository;
    private final EvaluationRepository evaluationRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StudentWithEvaluationDTO> getStudentsWithEvaluations(Long departmentId) {
        log.info("Récupération des étudiants avec évaluations pour le département {}", departmentId);

        // Vérifier que le département existe
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Département non trouvé avec l'ID: " + departmentId);
        }

        List<User> students = userRepository.findByDepartmentDepartmentIdAndRole(departmentId, Role.STUDENT);

        List<StudentWithEvaluationDTO> result = new ArrayList<>();

        for (User student : students) {
            // Récupérer le dossier PFA de l'étudiant (findByStudentUserId retourne une List)
            List<PFADossier> pfaList = pfaDossierRepository.findByStudentUserId(student.getUserId());

            StudentWithEvaluationDTO dto = StudentWithEvaluationDTO.builder()
                    .studentId(student.getUserId())
                    .studentFirstName(student.getFirstName())
                    .studentLastName(student.getLastName())
                    .studentEmail(student.getEmail())
                    .build();

            // Vérifier si l'étudiant a un dossier PFA
            if (!pfaList.isEmpty()) {
                PFADossier pfa = pfaList.get(0); // Prendre le premier (normalement un étudiant = un PFA)
                dto.setPfaId(pfa.getPfaId());
                dto.setPfaTitle(pfa.getTitle());
                dto.setPfaStatus(pfa.getCurrentStatus().name());

                // Récupérer le superviseur
                if (pfa.getSupervisor() != null) {
                    dto.setSupervisorId(pfa.getSupervisor().getUserId());
                    dto.setSupervisorFirstName(pfa.getSupervisor().getFirstName());
                    dto.setSupervisorLastName(pfa.getSupervisor().getLastName());
                }

                Optional<Evaluation> evalOptional = evaluationRepository.findByPfaDossierPfaId(pfa.getPfaId());
                if (evalOptional.isPresent()) {
                    Evaluation eval = evalOptional.get();
                    dto.setEvaluationId(eval.getEvaluationId());
                    dto.setTotalScore(eval.getTotalScore());
                }
            }

            result.add(dto);
        }

        log.info("Récupération de {} étudiants pour le département {}", result.size(), departmentId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessorWithStatsDTO> getProfessorsWithStats(Long departmentId) {
        log.info("Récupération des professeurs avec stats pour le département {}", departmentId);

        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Département non trouvé avec l'ID: " + departmentId);
        }

        List<User> professors = userRepository.findByDepartmentDepartmentIdAndRole(departmentId, Role.PROFESSOR);

        List<ProfessorWithStatsDTO> result = new ArrayList<>();

        for (User professor : professors) {
            List<PFADossier> supervisedPFAs = pfaDossierRepository.findBySupervisorUserId(professor.getUserId());
            int studentCount = supervisedPFAs.size();

            Double averageScore = null;
            if (studentCount > 0) {
                List<Double> scores = new ArrayList<>();
                for (PFADossier pfa : supervisedPFAs) {
                    Optional<Evaluation> evalOptional = evaluationRepository.findByPfaDossierPfaId(pfa.getPfaId());
                    if (evalOptional.isPresent() && evalOptional.get().getTotalScore() != null) {
                        scores.add(evalOptional.get().getTotalScore());
                    }
                }

                if (!scores.isEmpty()) {
                    averageScore = scores.stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                }
            }

            ProfessorWithStatsDTO dto = ProfessorWithStatsDTO.builder()
                    .professorId(professor.getUserId())
                    .professorFirstName(professor.getFirstName())
                    .professorLastName(professor.getLastName())
                    .professorEmail(professor.getEmail())
                    .phoneNumber(professor.getPhoneNumber())
                    .studentCount(studentCount)
                    .averageScore(averageScore)
                    .build();

            result.add(dto);
        }

        log.info("Récupération de {} professeurs pour le département {}", result.size(), departmentId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessorAssignmentDTO> getAssignments(Long departmentId) {
        log.info("Récupération des affectations pour le département {}", departmentId);

        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Département non trouvé avec l'ID: " + departmentId);
        }

        List<User> professors = userRepository.findByDepartmentDepartmentIdAndRole(departmentId, Role.PROFESSOR);

        List<ProfessorAssignmentDTO> result = new ArrayList<>();

        for (User professor : professors) {
            List<PFADossier> supervisedPFAs = pfaDossierRepository.findBySupervisorUserId(professor.getUserId());

            List<AssignedStudentDTO> assignedStudents = supervisedPFAs.stream()
                    .map(pfa -> {
                        User student = pfa.getStudent();
                        return AssignedStudentDTO.builder()
                                .studentId(student.getUserId())
                                .studentFirstName(student.getFirstName())
                                .studentLastName(student.getLastName())
                                .studentEmail(student.getEmail())
                                .pfaId(pfa.getPfaId())
                                .pfaTitle(pfa.getTitle())
                                .build();
                    })
                    .collect(Collectors.toList());

            ProfessorAssignmentDTO dto = ProfessorAssignmentDTO.builder()
                    .professorId(professor.getUserId())
                    .professorFirstName(professor.getFirstName())
                    .professorLastName(professor.getLastName())
                    .professorEmail(professor.getEmail())
                    .students(assignedStudents)
                    .build();

            result.add(dto);
        }

        log.info("Récupération de {} affectations pour le département {}", result.size(), departmentId);
        return result;
    }
    @Override
    @Transactional
    public Integer autoAssignStudentsToProfessors(Long departmentId) {
        log.info("Affectation automatique pour le département {}", departmentId);

        // Vérifier que le département existe
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Département non trouvé avec l'ID: " + departmentId);
        }

        List<User> professors = userRepository.findByDepartmentDepartmentIdAndRole(departmentId, Role.PROFESSOR);

        if (professors.isEmpty()) {
            throw new BadRequestException("Aucun encadrant disponible dans ce département");
        }

        List<User> allStudents = userRepository.findByDepartmentDepartmentIdAndRole(departmentId, Role.STUDENT);

        if (allStudents.isEmpty()) {
            log.info("Aucun étudiant dans ce département");
            return 0;
        }

        List<User> studentsToAssign = new ArrayList<>();
        for (User student : allStudents) {
            List<PFADossier> existingPFAs = pfaDossierRepository.findByStudentUserId(student.getUserId());
            boolean isAlreadyAssigned = existingPFAs.stream()
                    .anyMatch(pfa -> pfa.getSupervisor() != null);

            if (!isAlreadyAssigned) {
                studentsToAssign.add(student);
            }
        }

        if (studentsToAssign.isEmpty()) {
            log.info("Tous les étudiants sont déjà affectés à un encadrant");
            return 0;
        }

        log.info("Affectation de {} étudiants non affectés à {} professeurs",
                studentsToAssign.size(), professors.size());

        Collections.shuffle(studentsToAssign);
        Collections.shuffle(professors);

        int professorCount = professors.size();
        int assignedCount = 0;

        for (int i = 0; i < studentsToAssign.size(); i++) {
            User student = studentsToAssign.get(i);
            User professor = professors.get(i % professorCount);

            // Créer ou mettre à jour le dossier PFA
            PFADossier pfa = createOrUpdatePFADossier(student, professor);

            assignedCount++;
            log.info("Étudiant {} {} affecté au professeur {} {}",
                    student.getFirstName(), student.getLastName(),
                    professor.getFirstName(), professor.getLastName());
        }

        log.info("{} étudiants affectés avec succès", assignedCount);
        return assignedCount;
    }

    private PFADossier createOrUpdatePFADossier(User student, User professor) {
        List<PFADossier> existingPFAs = pfaDossierRepository.findByStudentUserId(student.getUserId());

        PFADossier pfa;
        if (!existingPFAs.isEmpty()) {
            pfa = existingPFAs.get(0);
            pfa.setSupervisor(professor);
        } else {
            pfa = PFADossier.builder()
                    .student(student)
                    .supervisor(professor)
                    .title("PFA - " + student.getFirstName() + " " + student.getLastName())
                    .description("Projet de fin d'année - En attente de définition")
                    .currentStatus(PFAStatus.CONVENTION_PENDING)
                    .updatedAt(System.currentTimeMillis())
                    .build();
        }

        return pfaDossierRepository.save(pfa);
    }
}