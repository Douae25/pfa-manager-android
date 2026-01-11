package com.ensate.pfa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evaluation_id")
    private Long evaluationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pfa_id", nullable = false)
    private PFADossier pfaDossier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @Column(name = "date_evaluation")
    private Long dateEvaluation;

    @Column(name = "total_score")
    private Double totalScore;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EvaluationDetail> evaluationDetails;

    @PrePersist
    protected void onCreate() {
        this.dateEvaluation = System.currentTimeMillis();
    }
}
