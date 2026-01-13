package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class PFADossierRequest {

    @SerializedName("studentId")
    private Long studentId;

    @SerializedName("title")
    private String title;

    @SerializedName("supervisorId")
    private Long supervisorId;

    @SerializedName("description")
    private String description;

    public PFADossierRequest() {}

    public PFADossierRequest(Long studentId, String title, Long supervisorId, String description) {
        this.studentId = studentId;
        this.title = title;
        this.supervisorId = supervisorId;
        this.description = description;
    }

    // Getters and Setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Long getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
