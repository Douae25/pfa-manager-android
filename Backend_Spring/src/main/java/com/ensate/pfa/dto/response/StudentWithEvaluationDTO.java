package com.ensate.pfa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentWithEvaluationDTO {
    private Long studentId;
    private String studentFirstName;
    private String studentLastName;
    private String studentEmail;

    private Long pfaId;
    private String pfaTitle;
    private String pfaStatus;

    private Long supervisorId;
    private String supervisorFirstName;
    private String supervisorLastName;

    private Long evaluationId;
    private Double totalScore;
}