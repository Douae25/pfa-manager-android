package com.ensate.pfa.dto;

import com.ensate.pfa.entity.enums.ConventionState;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ConventionDto {
    private Long conventionId;
    private Long pfaId;
    private String companyName;
    private String companyAddress;
    private String companySupervisorName;
    private String companySupervisorEmail;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String scannedFileUri;
    private Boolean isValidated;
    private String adminComment;
    private ConventionState state;

    @JsonProperty("state")
    public String getFrontendState() {
        return switch (state) {
            case DEMAND_PENDING -> "PENDING";
            case DEMAND_APPROVED -> "GENERATED";
            case DEMAND_REJECTED -> "REFUSED";
            case SIGNED_UPLOADED -> "UPLOADED";
            case UPLOAD_REJECTED -> "REJECTED";
            case VALIDATED -> "VALIDATED";
        };
    }
}
