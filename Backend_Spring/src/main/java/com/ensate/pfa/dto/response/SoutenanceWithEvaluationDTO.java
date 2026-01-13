package com.ensate.pfa.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoutenanceWithEvaluationDTO {
  private Long soutenanceId;
  private Long pfaId;
  private String pfaTitle;
  private Long studentId;
  private String studentFirstName;
  private String studentLastName;
  private Long dateSoutenance;
  private String location;
  private String soutenanceStatus;
  private Double totalScore;
  private boolean isEvaluated;

  public String getStudentFullName() {
    return studentFirstName + " " + studentLastName;
  }
}