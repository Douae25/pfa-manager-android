package com.ensate.pfa.dto.request;

import com.ensate.pfa.entity.enums.DeliverableFileType;
import com.ensate.pfa.entity.enums.DeliverableType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliverableRequest {
    @NotNull(message = "PFA ID is required")
    private Long pfaId;

    @NotBlank(message = "File title is required")
    private String fileTitle;

    @NotBlank(message = "File URI is required")
    private String fileUri;

    @NotNull(message = "Deliverable type is required")
    private DeliverableType deliverableType;

    private DeliverableFileType fileType;
}
