// dto/response/DeliverableDTO.java
package com.ensate.pfa.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverableDTO {
  private Long deliverableId;
  private Long pfaId;
  private String fileTitle;
  private String fileUri;
  private String deliverableType;
  private String deliverableFileType; 
  private Long uploadedAt;
  private Boolean isValidated;

  // Infos étudiant (pour la liste)
  private Long studentId;
  private String studentFirstName;
  private String studentLastName;
  private String pfaTitle;
}