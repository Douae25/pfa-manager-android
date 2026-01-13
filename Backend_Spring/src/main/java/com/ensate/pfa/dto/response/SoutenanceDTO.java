package com.ensate.pfa.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoutenanceDTO {
  private Long soutenanceId;
  private Long pfaId;
  private String location;
  private Long dateSoutenance;
  private String status; 
  private Long createdAt;

  private String pfaTitle;
  private Long studentId;
  private String studentFirstName;
  private String studentLastName;

}