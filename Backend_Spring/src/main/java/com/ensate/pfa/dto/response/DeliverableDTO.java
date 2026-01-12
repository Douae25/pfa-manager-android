package com.ensate.pfa.dto.response;

import com.ensate.pfa.entity.enums.DeliverableType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverableDTO {
  private Long deliverableId;
  private String fileTitle;
  private String fileUri;
  private Long uploadedAt;
  private DeliverableType deliverableType;
}