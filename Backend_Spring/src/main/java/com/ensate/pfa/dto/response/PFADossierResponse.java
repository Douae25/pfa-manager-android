package com.ensate.pfa.dto.response;

import com.ensate.pfa.entity.enums.PFAStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PFADossierResponse {
    private Long pfaId;
    private Long studentId;
    private String studentName;
    private Long supervisorId;
    private String supervisorName;
    private String title;
    private String description;
    private PFAStatus currentStatus;
    private Long updatedAt;
    private ConventionResponse convention;
}
