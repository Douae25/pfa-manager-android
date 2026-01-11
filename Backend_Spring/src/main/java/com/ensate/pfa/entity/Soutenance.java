package com.ensate.pfa.entity;

import com.ensate.pfa.entity.enums.SoutenanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "soutenances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Soutenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "soutenance_id")
    private Long soutenanceId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pfa_id", nullable = false)
    private PFADossier pfaDossier;

    private String location;

    @Column(name = "date_soutenance")
    private Long dateSoutenance;

    @Enumerated(EnumType.STRING)
    private SoutenanceStatus status;

    @Column(name = "created_at")
    private Long createdAt;

    @ManyToMany
    @JoinTable(
        name = "soutenance_jury",
        joinColumns = @JoinColumn(name = "soutenance_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> juryMembers;

    @PrePersist
    protected void onCreate() {
        this.createdAt = System.currentTimeMillis();
        if (this.status == null) {
            this.status = SoutenanceStatus.PLANNED;
        }
    }
}
