package com.ensate.pfa.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConventionDTO {
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
  private String state; 
  private String adminComment;
}