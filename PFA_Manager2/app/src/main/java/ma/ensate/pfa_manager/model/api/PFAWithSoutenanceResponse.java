// model/api/PFAWithSoutenanceResponse.java
package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class PFAWithSoutenanceResponse {

    @SerializedName("pfaId")
    private Long pfaId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status;

    @SerializedName("studentId")
    private Long studentId;

    @SerializedName("studentFirstName")
    private String studentFirstName;

    @SerializedName("studentLastName")
    private String studentLastName;

    @SerializedName("soutenance")
    private SoutenanceResponse soutenance;

    @SerializedName("evaluation")
    private EvaluationResponse evaluation;

    // Getters
    public Long getPfaId() { return pfaId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public Long getStudentId() { return studentId; }
    public String getStudentFirstName() { return studentFirstName; }
    public String getStudentLastName() { return studentLastName; }
    public SoutenanceResponse getSoutenance() { return soutenance; }
    public EvaluationResponse getEvaluation() { return evaluation; }

    public boolean isPlanned() {
        return soutenance != null;
    }

    public boolean isEvaluated() {
        return evaluation != null && evaluation.getTotalScore() != null;
    }
}