package com.ensate.pfa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PFADossierRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    private Long supervisorId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
}
