package com.ensate.pfa.service.impl;

import com.ensate.pfa.dto.request.DeliverableRequest;
import com.ensate.pfa.dto.response.DeliverableResponse;
import com.ensate.pfa.entity.Deliverable;
import com.ensate.pfa.entity.PFADossier;
import com.ensate.pfa.entity.enums.DeliverableType;
import com.ensate.pfa.exception.ResourceNotFoundException;
import com.ensate.pfa.repository.DeliverableRepository;
import com.ensate.pfa.repository.PFADossierRepository;
import com.ensate.pfa.service.DeliverableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliverableServiceImpl implements DeliverableService {

    private final DeliverableRepository deliverableRepository;
    private final PFADossierRepository pfaDossierRepository;

    @Override
    public DeliverableResponse depositDeliverable(DeliverableRequest request) {
        PFADossier dossier = pfaDossierRepository.findById(request.getPfaId())
                .orElseThrow(() -> new ResourceNotFoundException("PFADossier", request.getPfaId()));

        Deliverable deliverable = new Deliverable();
        deliverable.setPfaDossier(dossier);
        deliverable.setFileTitle(request.getFileTitle());
        deliverable.setFileUri(request.getFileUri());
        deliverable.setDeliverableType(request.getDeliverableType());
        deliverable.setFileType(request.getFileType());
        deliverable.setUploadedAt(System.currentTimeMillis());
        deliverable.setIsValidated(false);

        Deliverable saved = deliverableRepository.save(deliverable);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliverableResponse getDeliverableById(Long id) {
        Deliverable deliverable = deliverableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deliverable", id));
        return mapToResponse(deliverable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliverableResponse> getDeliverablesByPfa(Long pfaId) {
        return deliverableRepository.findByPfaDossierPfaId(pfaId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliverableResponse> getDeliverablesByPfaAndType(Long pfaId, DeliverableType type) {
        return deliverableRepository.findByPfaDossierPfaIdAndDeliverableType(pfaId, type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DeliverableResponse mapToResponse(Deliverable deliverable) {
        return DeliverableResponse.builder()
                .deliverableId(deliverable.getDeliverableId())
                .pfaId(deliverable.getPfaDossier().getPfaId())
                .fileTitle(deliverable.getFileTitle())
                .fileUri(deliverable.getFileUri())
                .deliverableType(deliverable.getDeliverableType())
                .fileType(deliverable.getFileType())
                .uploadedAt(deliverable.getUploadedAt())
                .isValidated(deliverable.getIsValidated())
                .build();
    }
}
