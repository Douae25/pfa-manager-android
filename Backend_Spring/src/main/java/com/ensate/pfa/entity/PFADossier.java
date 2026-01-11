package com.ensate.pfa.entity;

import com.ensate.pfa.entity.enums.PFAStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "pfa_dossiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PFADossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pfa_id")
    private Long pfaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private PFAStatus currentStatus;

    @Column(name = "updated_at")
    private Long updatedAt;

    @OneToOne(mappedBy = "pfaDossier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Convention convention;

    @OneToMany(mappedBy = "pfaDossier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Deliverable> deliverables;

    @OneToOne(mappedBy = "pfaDossier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Soutenance soutenance;

    @OneToMany(mappedBy = "pfaDossier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Evaluation> evaluations;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = System.currentTimeMillis();
        if (this.currentStatus == null) {
            this.currentStatus = PFAStatus.CONVENTION_PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }
}
