package com.ensate.pfa.service;

import com.ensate.pfa.dto.request.PFADossierRequest;
import com.ensate.pfa.dto.response.PFADossierDTO;
import com.ensate.pfa.dto.response.PFADossierResponse;

import java.util.List;

public interface PFADossierService {
    // Student use cases 
    PFADossierResponse getDossierById(Long id);
    List<PFADossierResponse> getDossiersByStudent(Long studentId);
    PFADossierResponse createOrGetDossier(PFADossierRequest request);
    List<PFADossierResponse> getAllDossiers();
    
    // Sync operations
    PFADossierResponse createDossier(PFADossierRequest request);
    PFADossierResponse updateDossier(Long id, PFADossierRequest request);
    List<PFADossierDTO> getAllPFADossiers();
}
