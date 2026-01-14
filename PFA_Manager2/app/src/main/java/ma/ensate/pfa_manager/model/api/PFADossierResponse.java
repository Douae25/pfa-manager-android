package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class PFADossierResponse {

    @SerializedName("pfaId")
    private Long pfaId;

    @SerializedName("studentId")
    private Long studentId;

    @SerializedName("studentName")
    private String studentName;

    @SerializedName("supervisorId")
    private Long supervisorId;

    @SerializedName("supervisorName")
    private String supervisorName;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("currentStatus")
    private String currentStatus;

    @SerializedName("updatedAt")
    private Long updatedAt;

    @SerializedName("convention")
    private Object convention;

    public PFADossierResponse() {}

    // Getters and Setters
    public Long getPfaId() { return pfaId; }
    public void setPfaId(Long pfaId) { this.pfaId = pfaId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Long getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }

    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    public Object getConvention() { return convention; }
    public void setConvention(Object convention) { this.convention = convention; }
}
