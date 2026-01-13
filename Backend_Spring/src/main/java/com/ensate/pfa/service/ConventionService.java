package com.ensate.pfa.service;

import com.ensate.pfa.dto.request.ConventionRequest;
import com.ensate.pfa.dto.response.ConventionResponse;

public interface ConventionService {
    // Student use cases 
    ConventionResponse requestConvention(ConventionRequest request);
    ConventionResponse uploadSignedConvention(Long conventionId, String scannedFileUri);
    ConventionResponse getConventionByPfaId(Long pfaId);
    ConventionResponse getConventionById(Long id);
}
