


package com.ensate.pfa.service;

import com.ensate.pfa.dto.request.ConventionRequest;
import com.ensate.pfa.dto.response.ConventionResponse;
import com.ensate.pfa.dto.request.ConventionAdminActionRequest;
import com.ensate.pfa.entity.enums.ConventionState;
import java.util.List;
import com.ensate.pfa.dto.ConventionDto;

public interface ConventionService {
    // Student use cases 
    ConventionResponse requestConvention(ConventionRequest request);
    ConventionResponse uploadSignedConvention(Long conventionId, String scannedFileUri);
    ConventionResponse getConventionByPfaId(Long pfaId);
    ConventionResponse getConventionById(Long id);

    // ADMIN
    List<ConventionResponse> getConventionsByState(ConventionState state);
    ConventionResponse adminActionOnConvention(Long id, ConventionAdminActionRequest request);

    // Pour le frontend : toutes les conventions avec mapping d'état
    List<ConventionDto> getAllConventionsFrontend();
    
    // Sync operations
    List<ConventionResponse> getAllConventions();
    ConventionResponse updateConvention(Long id, ConventionRequest request);
}
