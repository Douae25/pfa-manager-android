package ma.ensate.pfa_manager.model.dto;

import androidx.room.Embedded;
import androidx.room.Relation;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;
import ma.ensate.pfa_manager.model.User;

public class StudentWithPFA {

    @Embedded
    public User student;

    @Relation(
            parentColumn = "user_id",
            entityColumn = "student_id"
    )
    public PFADossier pfaDossier;

    // ========== HELPER METHODS ==========

    public String getFullName() {
        if (student == null) return "N/A";
        return student.getFirst_name() + " " + student.getLast_name();
    }

    public String getInitials() {
        if (student == null) return "??";
        String firstName = student.getFirst_name();
        String lastName = student.getLast_name();
        String first = (firstName != null && !firstName.isEmpty()) ? firstName.substring(0, 1) : "";
        String last = (lastName != null && !lastName.isEmpty()) ? lastName.substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    public Long getStudentId() {
        return student != null ? student.getUser_id() : null;
    }

    public String getEmail() {
        return student != null ? student.getEmail() : "";
    }

    public String getPhoneNumber() {
        return student != null ? student.getPhone_number() : "";
    }

    public String getPFATitle() {
        if (pfaDossier == null) return "Aucun PFA assigné";
        return pfaDossier.getTitle();
    }

    // Alias pour compatibilité
    public String getPfaTitle() {
        return getPFATitle();
    }

    public String getPFAStatus() {
        if (pfaDossier == null) return "NON_ASSIGNE";
        return pfaDossier.getCurrent_status() != null
                ? pfaDossier.getCurrent_status().name()
                : "UNKNOWN";
    }

    // ✅ AJOUTÉ : Retourne l'enum PFAStatus directement
    public PFAStatus getPfaStatus() {
        return pfaDossier != null ? pfaDossier.getCurrent_status() : null;
    }

    public boolean hasPFA() {
        return pfaDossier != null;
    }
}