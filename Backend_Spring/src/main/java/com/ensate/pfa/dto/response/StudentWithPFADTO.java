package com.ensate.pfa.dto.response;

import com.ensate.pfa.entity.enums.PFAStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentWithPFADTO {
    // Infos étudiant
    private Long studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    
    // Infos PFA
    private Long pfaId;
    private String pfaTitle;
    private String pfaDescription;
    private PFAStatus pfaStatus;
    
    // Score évaluation
    private Double totalScore;
    private boolean evaluated;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean hasPFA() {
        return pfaId != null;
    }
}