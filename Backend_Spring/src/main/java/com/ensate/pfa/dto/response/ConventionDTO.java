package com.ensate.pfa.dto.response;

import com.ensate.pfa.entity.enums.ConventionState;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConventionDTO {
  private Long conventionId;
  private String companyName;
  private String companyAddress;
  private ConventionState state;
  private String scannedFileUri;
}