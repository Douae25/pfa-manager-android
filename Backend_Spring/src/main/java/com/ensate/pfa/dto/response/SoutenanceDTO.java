package com.ensate.pfa.dto.response;

import com.ensate.pfa.entity.enums.SoutenanceStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoutenanceDTO {
  private Long soutenanceId;
  private Long dateSoutenance;
  private String location;
  private SoutenanceStatus status;
}