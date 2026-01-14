package com.ensate.pfa.service.impl;

import com.ensate.pfa.dto.request.ConventionRequest;
import com.ensate.pfa.dto.response.ConventionResponse;
import com.ensate.pfa.entity.Convention;
import com.ensate.pfa.entity.PFADossier;
import com.ensate.pfa.entity.enums.ConventionState;
import com.ensate.pfa.exception.BadRequestException;
import com.ensate.pfa.exception.ResourceNotFoundException;
import com.ensate.pfa.repository.ConventionRepository;
import com.ensate.pfa.repository.PFADossierRepository;
import com.ensate.pfa.repository.UserRepository;
import com.ensate.pfa.service.ConventionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Update PFA Dossier title and description if provided
        if (request.getPfaTitle() != null && !request.getPfaTitle().isEmpty()) {
            dossier.setTitle(request.getPfaTitle());
        }
        if (request.getPfaDescription() != null && !request.getPfaDescription().isEmpty()) {
            dossier.setDescription(request.getPfaDescription());
        }
        dossier.setUpdatedAt(System.currentTimeMillis());

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
}

