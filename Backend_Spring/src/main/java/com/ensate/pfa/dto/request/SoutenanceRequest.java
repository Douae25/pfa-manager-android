// dto/request/SoutenanceRequest.java
package com.ensate.pfa.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoutenanceRequest {
  private Long pfaId;
  private String location;
  private Long dateSoutenance;
}