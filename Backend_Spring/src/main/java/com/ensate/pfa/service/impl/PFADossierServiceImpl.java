package com.ensate.pfa.service.impl;

import com.ensate.pfa.dto.request.PFADossierRequest;
import com.ensate.pfa.dto.response.ConventionResponse;
import com.ensate.pfa.dto.response.PFADossierDTO;
import com.ensate.pfa.dto.response.PFADossierResponse;
import com.ensate.pfa.entity.PFADossier;
import com.ensate.pfa.entity.User;
import com.ensate.pfa.entity.enums.PFAStatus;
import com.ensate.pfa.exception.ResourceNotFoundException;
import com.ensate.pfa.repository.PFADossierRepository;
import com.ensate.pfa.repository.UserRepository;
import com.ensate.pfa.service.PFADossierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PFADossierServiceImpl implements PFADossierService {

    private final PFADossierRepository pfaDossierRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PFADossierResponse getDossierById(Long id) {
        PFADossier dossier = pfaDossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PFADossier", id));
        return mapToResponse(dossier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PFADossierResponse> getDossiersByStudent(Long studentId) {
        return pfaDossierRepository.findByStudentUserId(studentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PFADossierResponse createOrGetDossier(PFADossierRequest request) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", request.getStudentId()));

        List<PFADossier> existingDossiers = pfaDossierRepository.findByStudentUserId(request.getStudentId());
        
        PFADossier activeDossier = existingDossiers.stream()
                .filter(d -> d.getCurrentStatus() == PFAStatus.CONVENTION_PENDING)
                .sorted((d1, d2) -> Long.compare(d2.getUpdatedAt(), d1.getUpdatedAt()))
                .findFirst()
                .orElse(null);

        if (activeDossier != null) {
            if (request.getTitle() != null && !request.getTitle().isEmpty()) {
                activeDossier.setTitle(request.getTitle());
            }
            if (request.getDescription() != null && !request.getDescription().isEmpty()) {
                activeDossier.setDescription(request.getDescription());
            }
            pfaDossierRepository.save(activeDossier);
            return mapToResponse(activeDossier);
        }

        PFADossier newDossier = new PFADossier();
        newDossier.setStudent(student);
        newDossier.setTitle(request.getTitle());
        newDossier.setDescription(request.getDescription());
        newDossier.setCurrentStatus(PFAStatus.CONVENTION_PENDING);
        
        if (request.getSupervisorId() != null) {
            User supervisor = userRepository.findById(request.getSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor", request.getSupervisorId()));
            newDossier.setSupervisor(supervisor);
        }

        PFADossier savedDossier = pfaDossierRepository.save(newDossier);
        return mapToResponse(savedDossier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PFADossierResponse> getAllDossiers() {
        return pfaDossierRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PFADossierResponse createDossier(PFADossierRequest request) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", request.getStudentId()));

        PFADossier newDossier = new PFADossier();
        newDossier.setStudent(student);
        newDossier.setTitle(request.getTitle());
        newDossier.setDescription(request.getDescription());
        newDossier.setCurrentStatus(request.getCurrentStatus() != null ? request.getCurrentStatus() : PFAStatus.CONVENTION_PENDING);
        
        if (request.getSupervisorId() != null) {
            User supervisor = userRepository.findById(request.getSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor", request.getSupervisorId()));
            newDossier.setSupervisor(supervisor);
        }

        PFADossier savedDossier = pfaDossierRepository.save(newDossier);
        return mapToResponse(savedDossier);
    }

    @Override
    public PFADossierResponse updateDossier(Long id, PFADossierRequest request) {
        PFADossier dossier = pfaDossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PFADossier", id));

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            dossier.setTitle(request.getTitle());
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            dossier.setDescription(request.getDescription());
        }
        if (request.getCurrentStatus() != null) {
            dossier.setCurrentStatus(request.getCurrentStatus());
        }
        if (request.getSupervisorId() != null) {
            User supervisor = userRepository.findById(request.getSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor", request.getSupervisorId()));
            dossier.setSupervisor(supervisor);
        }

        PFADossier updatedDossier = pfaDossierRepository.save(dossier);
        return mapToResponse(updatedDossier);
    }

    private PFADossierResponse mapToResponse(PFADossier dossier) {
        PFADossierResponse.PFADossierResponseBuilder builder = PFADossierResponse.builder()
                .pfaId(dossier.getPfaId())
                .studentId(dossier.getStudent().getUserId())
                .studentName(dossier.getStudent().getFirstName() + " " + dossier.getStudent().getLastName())
                .title(dossier.getTitle())
                .description(dossier.getDescription())
                .currentStatus(dossier.getCurrentStatus())
                .updatedAt(dossier.getUpdatedAt());

        if (dossier.getSupervisor() != null) {
            builder.supervisorId(dossier.getSupervisor().getUserId())
                    .supervisorName(dossier.getSupervisor().getFirstName() + " " + dossier.getSupervisor().getLastName());
        }

        if (dossier.getConvention() != null) {
            builder.convention(ConventionResponse.builder()
                    .conventionId(dossier.getConvention().getConventionId())
                    .pfaId(dossier.getPfaId())
                    .companyName(dossier.getConvention().getCompanyName())
                    .companyAddress(dossier.getConvention().getCompanyAddress())
                    .companySupervisorName(dossier.getConvention().getCompanySupervisorName())
                    .companySupervisorEmail(dossier.getConvention().getCompanySupervisorEmail())
                    .startDate(dossier.getConvention().getStartDate())
                    .endDate(dossier.getConvention().getEndDate())
                    .scannedFileUri(dossier.getConvention().getScannedFileUri())
                    .isValidated(dossier.getConvention().getIsValidated())
                    .state(dossier.getConvention().getState())
                    .build());
        }

        return builder.build();
    }

    @Override
    public List<PFADossierDTO> getAllPFADossiers() {
        return pfaDossierRepository.findAll().stream()
                .map(pfa -> PFADossierDTO.builder()
                        .pfaId(pfa.getPfaId())
                        .title(pfa.getTitle())
                        .description(pfa.getDescription())
                        .currentStatus(pfa.getCurrentStatus().name())
                        .studentId(pfa.getStudent() != null ?
                                pfa.getStudent().getUserId() : null)
                        .studentName(pfa.getStudent() != null ?
                                pfa.getStudent().getFirstName() + " " +
                                        pfa.getStudent().getLastName() : null)
                        .supervisorId(pfa.getSupervisor() != null ?
                                pfa.getSupervisor().getUserId() : null)
                        .supervisorName(pfa.getSupervisor() != null ?
                                pfa.getSupervisor().getFirstName() + " " +
                                        pfa.getSupervisor().getLastName() : null)
                        .updatedAt(pfa.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
