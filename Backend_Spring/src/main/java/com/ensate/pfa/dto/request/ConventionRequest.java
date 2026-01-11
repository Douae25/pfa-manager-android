package com.ensate.pfa.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConventionRequest {
    @NotNull(message = "PFA Dossier ID is required")
    private Long pfaId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String companyAddress;

    private String companySupervisorName;

    @Email(message = "Invalid email format")
    private String companySupervisorEmail;

    private Long startDate;
    private Long endDate;
}

