package ma.ensate.pfa_manager.model.dto;

import ma.ensate.pfa_manager.model.Evaluation;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.Soutenance;
import ma.ensate.pfa_manager.model.User;

public class SoutenanceWithEvaluation {
    public Soutenance soutenance;
    public PFADossier pfa;
    public User student;
    public Evaluation evaluation;

    public SoutenanceWithEvaluation() {}

    public SoutenanceWithEvaluation(Soutenance soutenance, PFADossier pfa, User student, Evaluation evaluation) {
        this.soutenance = soutenance;
        this.pfa = pfa;
        this.student = student;
        this.evaluation = evaluation;
    }

    public boolean isEvaluated() {
        return evaluation != null;
    }

    public String getStudentFullName() {
        if (student == null) return "Étudiant inconnu";
        return student.getFirst_name() + " " + student.getLast_name();
    }

    public String getStudentInitials() {
        if (student == null) return "??";
        String first = student.getFirst_name() != null && !student.getFirst_name().isEmpty()
                ? student.getFirst_name().substring(0, 1) : "";
        String last = student.getLast_name() != null && !student.getLast_name().isEmpty()
                ? student.getLast_name().substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    public String getPfaTitle() {
        return pfa != null ? pfa.getTitle() : "Projet non défini";
    }

    public Long getSoutenanceId() {
        return soutenance != null ? soutenance.getSoutenance_id() : null;
    }

    public Long getPfaId() {
        return pfa != null ? pfa.getPfa_id() : null;
    }

    public Long getDateSoutenance() {
        return soutenance != null ? soutenance.getDate_soutenance() : null;
    }

    public String getLocation() {
        return soutenance != null ? soutenance.getLocation() : "";
    }

    public Double getTotalScore() {
        return evaluation != null ? evaluation.getTotal_score() : null;
    }
}