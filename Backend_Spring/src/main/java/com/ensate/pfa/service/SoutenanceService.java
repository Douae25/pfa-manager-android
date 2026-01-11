package com.ensate.pfa.service;

import com.ensate.pfa.dto.response.SoutenanceResponse;

public interface SoutenanceService {
    // Student use case: Consult defense date
    SoutenanceResponse getSoutenanceByPfaId(Long pfaId);
}
