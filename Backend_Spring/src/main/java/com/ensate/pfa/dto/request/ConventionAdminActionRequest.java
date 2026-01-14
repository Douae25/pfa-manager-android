package com.ensate.pfa.dto.request;

import lombok.Data;

@Data
public class ConventionAdminActionRequest {
    private String action; // "ACCEPT" ou "REFUSE"
    private String comment; // optionnel, raison du refus
}
