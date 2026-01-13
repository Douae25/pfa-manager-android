package com.ensate.pfa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PFADossierDTO {
    private Long pfaId;
    private String title;
    private String description;
    private String currentStatus;
    private Long studentId;
    private String studentName;
    private Long supervisorId;
    private String supervisorName;
    private Long updatedAt;
}