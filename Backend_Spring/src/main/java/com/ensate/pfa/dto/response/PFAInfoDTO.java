package com.ensate.pfa.dto.response;

import com.ensate.pfa.entity.enums.PFAStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PFAInfoDTO {
  private Long pfaId;
  private String title;
  private String description;
  private PFAStatus currentStatus;
  private Long updatedAt;
}