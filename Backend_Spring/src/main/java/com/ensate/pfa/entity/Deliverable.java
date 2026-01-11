package com.ensate.pfa.entity;

import com.ensate.pfa.entity.enums.DeliverableFileType;
import com.ensate.pfa.entity.enums.DeliverableType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "deliverables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deliverable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deliverable_id")
    private Long deliverableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pfa_id", nullable = false)
    private PFADossier pfaDossier;

    @Column(name = "file_title", nullable = false)
    private String fileTitle;

    @Column(name = "file_uri", nullable = false)
    private String fileUri;

    @Enumerated(EnumType.STRING)
    @Column(name = "deliverable_type", nullable = false)
    private DeliverableType deliverableType;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type")
    private DeliverableFileType fileType;

    @Column(name = "uploaded_at")
    private Long uploadedAt;

    @Column(name = "is_validated")
    private Boolean isValidated;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = System.currentTimeMillis();
        if (this.isValidated == null) {
            this.isValidated = false;
        }
    }
}
