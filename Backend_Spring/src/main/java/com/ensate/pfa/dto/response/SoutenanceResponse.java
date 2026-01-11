package com.ensate.pfa.dto.response;

import com.ensate.pfa.entity.enums.SoutenanceStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SoutenanceResponse {
    private Long soutenanceId;
    private Long pfaId;
    private String studentName;
    private String pfaTitle;
    private String location;
    private Long dateSoutenance;
    private SoutenanceStatus status;
    private Long createdAt;
    private List<UserResponse> juryMembers;
}
