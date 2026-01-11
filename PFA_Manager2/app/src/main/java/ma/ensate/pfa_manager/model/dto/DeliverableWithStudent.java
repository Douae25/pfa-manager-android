package ma.ensate.pfa_manager.model.dto;

import ma.ensate.pfa_manager.model.Deliverable;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.User;

public class DeliverableWithStudent {
    public Deliverable deliverable;
    public PFADossier pfa;
    public User student;

    public DeliverableWithStudent() {}

    public DeliverableWithStudent(Deliverable deliverable, PFADossier pfa, User student) {
        this.deliverable = deliverable;
        this.pfa = pfa;
        this.student = student;
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

    public Long getDeliverableId() {
        return deliverable != null ? deliverable.getDeliverable_id() : null;
    }

    public String getFileTitle() {
        return deliverable != null ? deliverable.getFile_title() : "";
    }

    public String getFileUri() {
        return deliverable != null ? deliverable.getFile_uri() : "";
    }

    public Long getUploadedAt() {
        return deliverable != null ? deliverable.getUploaded_at() : null;
    }
}