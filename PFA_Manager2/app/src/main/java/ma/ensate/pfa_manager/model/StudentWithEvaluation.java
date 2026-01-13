package ma.ensate.pfa_manager.model;

public class StudentWithEvaluation {
    private User student;
    private Evaluation evaluation;
    private User supervisor;
    private PFADossier pfaDossier;

    public StudentWithEvaluation() {}

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Evaluation getEvaluation() { return evaluation; }
    public void setEvaluation(Evaluation evaluation) { this.evaluation = evaluation; }

    public User getSupervisor() { return supervisor; }
    public void setSupervisor(User supervisor) { this.supervisor = supervisor; }

    public PFADossier getPfaDossier() { return pfaDossier; }
    public void setPfaDossier(PFADossier pfaDossier) { this.pfaDossier = pfaDossier; }

    public String getFullName() {
        if (student != null) {
            return student.getFirst_name() + " " + student.getLast_name();
        }
        return "";
    }

    public String getInitials() {
        if (student != null && student.getFirst_name() != null && student.getLast_name() != null) {
            return String.valueOf(student.getFirst_name().charAt(0)) +
                    String.valueOf(student.getLast_name().charAt(0));
        }
        return "??";
    }

    public Double getScore() {
        return evaluation != null ? evaluation.getTotal_score() : null;
    }

    public String getScoreText() {
        Double score = getScore();
        return score != null ? String.format("%.1f", score) : "N/A";
    }

    public String getSupervisorName() {
        if (supervisor != null) {
            return supervisor.getFirst_name() + " " + supervisor.getLast_name();
        }
        return "Non assigné";
    }

    public String getProjectTitle() {
        return pfaDossier != null && pfaDossier.getTitle() != null ?
                pfaDossier.getTitle() : "Pas de projet";
    }

    public String getStatus() {
        if (pfaDossier != null && pfaDossier.getCurrent_status() != null) {
            return pfaDossier.getCurrent_status().name();
        }
        return "INCONNU";
    }

    public int getStatusColor() {
        String status = getStatus();
        switch (status) {
            case "IN_PROGRESS": return 0xFF4CAF50;
            case "CLOSED": return 0xFF2196F3;
            case "CONVENTION_PENDING": return 0xFFFF9800;
            default: return 0xFF9E9E9E;
        }
    }
}