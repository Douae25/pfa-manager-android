package ma.ensate.pfa_manager.model;

public class ProfessorWithStats {
    private User professor;
    private int studentCount;
    private Double averageScore;

    public ProfessorWithStats() {}

    public User getProfessor() { return professor; }
    public void setProfessor(User professor) { this.professor = professor; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public String getFullName() {
        if (professor != null) {
            return professor.getFirst_name() + " " + professor.getLast_name();
        }
        return "";
    }

    public String getInitials() {
        if (professor != null && professor.getFirst_name() != null && professor.getLast_name() != null) {
            return String.valueOf(professor.getFirst_name().charAt(0)) +
                    String.valueOf(professor.getLast_name().charAt(0));
        }
        return "??";
    }

    public String getAverageScoreText() {
        return averageScore != null ? String.format("%.1f", averageScore) : "N/A";
    }
}