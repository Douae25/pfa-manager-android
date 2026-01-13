package com.ensate.pfa.service.impl;

import com.ensate.pfa.dto.request.ConventionRequest;
import com.ensate.pfa.dto.response.ConventionResponse;
import com.ensate.pfa.entity.Convention;
import com.ensate.pfa.entity.PFADossier;
import com.ensate.pfa.entity.enums.ConventionState;
import com.ensate.pfa.dto.request.ConventionAdminActionRequest;
import com.ensate.pfa.exception.BadRequestException;
import com.ensate.pfa.exception.ResourceNotFoundException;
import com.ensate.pfa.repository.ConventionRepository;
import com.ensate.pfa.repository.PFADossierRepository;
import com.ensate.pfa.repository.UserRepository;
import com.ensate.pfa.service.ConventionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional
public class ConventionServiceImpl implements ConventionService {

    private final ConventionRepository conventionRepository;
    private final PFADossierRepository pfaDossierRepository;
    private final UserRepository userRepository;

    @Override
    public ConventionResponse requestConvention(ConventionRequest request) {
        userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", request.getStudentId()));

        PFADossier dossier = pfaDossierRepository.findById(request.getPfaId())
                .orElseThrow(() -> new ResourceNotFoundException("PFA Dossier", request.getPfaId()));

        if (!dossier.getStudent().getUserId().equals(request.getStudentId())) {
            throw new BadRequestException("This PFA dossier does not belong to you");
        }

        if (dossier.getConvention() != null) {
            throw new BadRequestException("Convention already exists for this PFA dossier");
        }

        Convention convention = new Convention();
        convention.setPfaDossier(dossier);
        convention.setCompanyName(request.getCompanyName());
        convention.setCompanyAddress(request.getCompanyAddress());
        convention.setCompanySupervisorName(request.getCompanySupervisorName());
        convention.setCompanySupervisorEmail(request.getCompanySupervisorEmail());
        convention.setStartDate(request.getStartDate());
        convention.setEndDate(request.getEndDate());
        convention.setState(ConventionState.DEMAND_PENDING);
        convention.setIsValidated(false);

        Convention saved = conventionRepository.save(convention);
        
        dossier.setConvention(saved);
        pfaDossierRepository.save(dossier);

        return mapToResponse(saved);
    }

    @Override
    public ConventionResponse uploadSignedConvention(Long conventionId, String scannedFileUri) {
        Convention convention = conventionRepository.findById(conventionId)
                .orElseThrow(() -> new ResourceNotFoundException("Convention", conventionId));

        if (convention.getState() != ConventionState.DEMAND_APPROVED) {
            throw new BadRequestException("Convention must be approved before uploading signed document");
        }

        convention.setScannedFileUri(scannedFileUri);
        convention.setState(ConventionState.SIGNED_UPLOADED);

        Convention saved = conventionRepository.save(convention);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ConventionResponse getConventionByPfaId(Long pfaId) {
        Convention convention = conventionRepository.findByPfaDossierPfaId(pfaId)
                .orElseThrow(() -> new ResourceNotFoundException("Convention not found for PFA: " + pfaId));
        return mapToResponse(convention);
    }

    @Override
    @Transactional(readOnly = true)
    public ConventionResponse getConventionById(Long id) {
        Convention convention = conventionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convention", id));
        return mapToResponse(convention);
    }

    private ConventionResponse mapToResponse(Convention convention) {
        return ConventionResponse.builder()
                .conventionId(convention.getConventionId())
                .pfaId(convention.getPfaDossier().getPfaId())
                .companyName(convention.getCompanyName())
                .companyAddress(convention.getCompanyAddress())
                .companySupervisorName(convention.getCompanySupervisorName())
                .companySupervisorEmail(convention.getCompanySupervisorEmail())
                .startDate(convention.getStartDate())
                .endDate(convention.getEndDate())
                .scannedFileUri(convention.getScannedFileUri())
                .isValidated(convention.getIsValidated())
                .state(convention.getState())
                .adminComment(convention.getAdminComment())
                .build();
    }

    // ADMIN: Lister conventions par état
    @Override
    @Transactional(readOnly = true)
    public java.util.List<ConventionResponse> getConventionsByState(ConventionState state) {
        return conventionRepository.findByState(state)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ADMIN: Traiter une convention (accept/refuse)
    @Override
    public ConventionResponse adminActionOnConvention(Long id, ConventionAdminActionRequest request) {
        Convention convention = conventionRepository.findByIdWithPfaDossier(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convention", id));

        String action = request.getAction();
        String comment = request.getComment();

        // Gestion des transitions d'état
        switch (convention.getState()) {
            case DEMAND_PENDING -> {
                if ("ACCEPT".equalsIgnoreCase(action)) {
                    convention.setState(ConventionState.DEMAND_APPROVED);
                    convention.setAdminComment(null);
                } else if ("REFUSE".equalsIgnoreCase(action)) {
                    convention.setState(ConventionState.DEMAND_REJECTED);
                    convention.setAdminComment(comment);
                } else {
                    throw new BadRequestException("Invalid action for DEMAND_PENDING");
                }
            }
            case SIGNED_UPLOADED -> {
                if ("ACCEPT".equalsIgnoreCase(action)) {
                    convention.setState(ConventionState.VALIDATED);
                    convention.setIsValidated(true);
                    convention.setAdminComment(null);
                    // Update PFADossier status to IN_PROGRESS
                    PFADossier dossier = convention.getPfaDossier();
                    if (dossier != null) {
                        dossier.setCurrentStatus(com.ensate.pfa.entity.enums.PFAStatus.IN_PROGRESS);
                        pfaDossierRepository.save(dossier);
                    }
                } else if ("REFUSE".equalsIgnoreCase(action)) {
                    convention.setState(ConventionState.UPLOAD_REJECTED);
                    convention.setAdminComment(comment);
                } else {
                    throw new BadRequestException("Invalid action for SIGNED_UPLOADED");
                }
            }
            default -> throw new BadRequestException("Action not allowed for this convention state");
        }

        Convention saved = conventionRepository.save(convention);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.ensate.pfa.dto.ConventionDto> getAllConventionsFrontend() {
        return conventionRepository.findAll().stream().map(this::mapToDtoFrontend).toList();
    }

    private com.ensate.pfa.dto.ConventionDto mapToDtoFrontend(Convention convention) {
        com.ensate.pfa.dto.ConventionDto dto = new com.ensate.pfa.dto.ConventionDto();
        dto.setConventionId(convention.getConventionId());
        dto.setPfaId(convention.getPfaDossier().getPfaId());
        dto.setCompanyName(convention.getCompanyName());
        dto.setCompanyAddress(convention.getCompanyAddress());
        dto.setCompanySupervisorName(convention.getCompanySupervisorName());
        dto.setCompanySupervisorEmail(convention.getCompanySupervisorEmail());
        // Convert Long timestamp to LocalDate
        if (convention.getStartDate() != null) {
            LocalDate startDate = Instant.ofEpochMilli(convention.getStartDate()).atZone(ZoneId.systemDefault()).toLocalDate();
            dto.setStartDate(startDate);
        }
        if (convention.getEndDate() != null) {
            LocalDate endDate = Instant.ofEpochMilli(convention.getEndDate()).atZone(ZoneId.systemDefault()).toLocalDate();
            dto.setEndDate(endDate);
        }
        dto.setScannedFileUri(convention.getScannedFileUri());
        dto.setIsValidated(convention.getIsValidated());
        dto.setAdminComment(convention.getAdminComment());
        dto.setState(convention.getState());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConventionResponse> getAllConventions() {
        return conventionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ConventionResponse updateConvention(Long id, ConventionRequest request) {
        Convention convention = conventionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convention", id));

        if (request.getCompanyName() != null && !request.getCompanyName().isEmpty()) {
            convention.setCompanyName(request.getCompanyName());
        }
        if (request.getCompanyAddress() != null && !request.getCompanyAddress().isEmpty()) {
            convention.setCompanyAddress(request.getCompanyAddress());
        }
        if (request.getCompanySupervisorName() != null && !request.getCompanySupervisorName().isEmpty()) {
            convention.setCompanySupervisorName(request.getCompanySupervisorName());
        }
        if (request.getCompanySupervisorEmail() != null && !request.getCompanySupervisorEmail().isEmpty()) {
            convention.setCompanySupervisorEmail(request.getCompanySupervisorEmail());
        }
        if (request.getStartDate() != null) {
            convention.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            convention.setEndDate(request.getEndDate());
        }

        Convention updatedConvention = conventionRepository.save(convention);
        return mapToResponse(updatedConvention);
    }}