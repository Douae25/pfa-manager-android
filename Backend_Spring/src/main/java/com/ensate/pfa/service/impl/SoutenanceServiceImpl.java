package com.ensate.pfa.service.impl;

import com.ensate.pfa.dto.response.SoutenanceResponse;
import com.ensate.pfa.entity.Soutenance;
import com.ensate.pfa.exception.ResourceNotFoundException;
import com.ensate.pfa.repository.SoutenanceRepository;
import com.ensate.pfa.service.SoutenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SoutenanceServiceImpl implements SoutenanceService {

    private final SoutenanceRepository soutenanceRepository;

    @Override
    @Transactional(readOnly = true)
    public SoutenanceResponse getSoutenanceByPfaId(Long pfaId) {
        Soutenance soutenance = soutenanceRepository.findByPfaDossierPfaId(pfaId)
                .orElseThrow(() -> new ResourceNotFoundException("Soutenance not found for PFA: " + pfaId));
        return mapToResponse(soutenance);
    }

    private SoutenanceResponse mapToResponse(Soutenance soutenance) {
        return SoutenanceResponse.builder()
                .soutenanceId(soutenance.getSoutenanceId())
                .pfaId(soutenance.getPfaDossier().getPfaId())
                .studentName(soutenance.getPfaDossier().getStudent().getFirstName() + " " + 
                             soutenance.getPfaDossier().getStudent().getLastName())
                .pfaTitle(soutenance.getPfaDossier().getTitle())
                .location(soutenance.getLocation())
                .dateSoutenance(soutenance.getDateSoutenance())
                .status(soutenance.getStatus())
                .createdAt(soutenance.getCreatedAt())
                .build();
    }
}
