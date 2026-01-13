package com.ensate.pfa.dto.response;

import com.ensate.pfa.entity.enums.DeliverableFileType;
import com.ensate.pfa.entity.enums.DeliverableType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliverableResponse {
    private Long deliverableId;
    private Long pfaId;
    private String fileTitle;
    private String fileUri;
    private DeliverableType deliverableType;
    private DeliverableFileType fileType;
    private Long uploadedAt;
    private Boolean isValidated;
}
