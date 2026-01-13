package com.ensate.pfa.entity;

import com.ensate.pfa.entity.enums.ConventionState;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conventions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Convention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "convention_id")
    private Long conventionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pfa_id", nullable = false)
    private PFADossier pfaDossier;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_address")
    private String companyAddress;

    @Column(name = "company_supervisor_name")
    private String companySupervisorName;

    @Column(name = "company_supervisor_email")
    private String companySupervisorEmail;

    @Column(name = "start_date")
    private Long startDate;

    @Column(name = "end_date")
    private Long endDate;

    @Column(name = "scanned_file_uri")
    private String scannedFileUri;

    @Column(name = "is_validated")
    private Boolean isValidated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConventionState state;

    @Column(name = "admin_comment", columnDefinition = "TEXT")
    private String adminComment;

    @PrePersist
    protected void onCreate() {
        if (this.state == null) {
            this.state = ConventionState.DEMAND_PENDING;
        }
        if (this.isValidated == null) {
            this.isValidated = false;
        }
    }
}
