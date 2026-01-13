package com.ensate.pfa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorWithStatsDTO {
    private Long professorId;
    private String professorFirstName;
    private String professorLastName;
    private String professorEmail;
    private String phoneNumber;

    private Integer studentCount;
    private Double averageScore;
}