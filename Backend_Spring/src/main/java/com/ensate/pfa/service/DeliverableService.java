package com.ensate.pfa.service;

import com.ensate.pfa.dto.request.DeliverableRequest;
import com.ensate.pfa.dto.response.DeliverableResponse;
import com.ensate.pfa.entity.enums.DeliverableType;

import java.util.List;

public interface DeliverableService {
    // Student use cases 
    DeliverableResponse depositDeliverable(DeliverableRequest request);
    DeliverableResponse getDeliverableById(Long id);
    List<DeliverableResponse> getDeliverablesByPfa(Long pfaId);
    List<DeliverableResponse> getDeliverablesByPfaAndType(Long pfaId, DeliverableType type);
}
