package ma.ensate.pfa_manager.model.dto;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;
import ma.ensate.pfa_manager.model.User;

public class StudentWithPFA {

    @Embedded
    public User student;

    @Relation(
            entity = PFADossier.class,
            parentColumn = "user_id",
            entityColumn = "student_id"
    )
    public PFADossierWithEvaluation pfaDetails;

    public StudentWithPFA() {}


    public String getFullName() {
        if (student == null) return "Inconnu";
        String firstName = student.getFirst_name() != null ? student.getFirst_name() : "";
        String lastName = student.getLast_name() != null ? student.getLast_name() : "";
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return student != null ? student.getEmail() : "";
    }

    private PFADossier getPfa() {
        return (pfaDetails != null) ? pfaDetails.pfaDossier : null;
    }

    public boolean hasPFA() {
        return getPfa() != null;
    }

    public String getPFATitle() {
        return hasPFA() ? getPfa().getTitle() : "Aucun projet";
    }

    public String getPFAStatus() {
        return hasPFA() && getPfa().getCurrent_status() != null
                ? getPfa().getCurrent_status().name() : null;
    }

    public Long getStudentId() {
        return student != null ? student.getUser_id() : null;
    }

    public Long getPfaId() {
        return hasPFA() ? getPfa().getPfa_id() : null;
    }

    // Helper pour récupérer l'évaluation
    public boolean isEvaluated() {
        return pfaDetails != null &&
                pfaDetails.evaluation != null &&
                pfaDetails.evaluation.getTotal_score() != null;
    }

    public Double getScore() {
        return isEvaluated() ? pfaDetails.evaluation.getTotal_score() : null;
    }

    public boolean isClosed() {
        return hasPFA() && getPfa().getCurrent_status() == PFAStatus.CLOSED;
    }

    public String getStudentInitials() {
        if (student == null) return "??";
        String firstName = student.getFirst_name();
        String lastName = student.getLast_name();
        String first = (firstName != null && !firstName.isEmpty()) ? firstName.substring(0, 1) : "";
        String last = (lastName != null && !lastName.isEmpty()) ? lastName.substring(0, 1) : "";
        return (first + last).toUpperCase();
    }
}