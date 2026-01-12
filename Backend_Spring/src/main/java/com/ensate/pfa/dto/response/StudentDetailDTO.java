package com.ensate.pfa.dto.response;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetailDTO {
  private Long studentId;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private PFAInfoDTO pfa;
  private ConventionDTO convention;
  private List<DeliverableDTO> deliverables;
  private SoutenanceDTO soutenance;
  private EvaluationDTO evaluation;
}