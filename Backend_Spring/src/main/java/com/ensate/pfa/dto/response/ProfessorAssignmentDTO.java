package com.ensate.pfa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorAssignmentDTO {
    private Long professorId;
    private String professorFirstName;
    private String professorLastName;
    private String professorEmail;

    private List<AssignedStudentDTO> students;
}