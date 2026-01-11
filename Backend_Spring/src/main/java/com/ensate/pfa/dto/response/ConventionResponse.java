package com.ensate.pfa.dto.response;

import com.ensate.pfa.entity.enums.ConventionState;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConventionResponse {
    private Long conventionId;
    private Long pfaId;
    private String companyName;
    private String companyAddress;
    private String companySupervisorName;
    private String companySupervisorEmail;
    private Long startDate;
    private Long endDate;
    private String scannedFileUri;
    private Boolean isValidated;
    private ConventionState state;
    private String adminComment;
}
