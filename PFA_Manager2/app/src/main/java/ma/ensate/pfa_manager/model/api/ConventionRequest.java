package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class ConventionRequest {

    @SerializedName("studentId")
    private Long studentId;

    @SerializedName("pfaId")
    private Long pfaId;

    @SerializedName("companyName")
    private String companyName;

    @SerializedName("companyAddress")
    private String companyAddress;

    @SerializedName("companySupervisorName")
    private String companySupervisorName;

    @SerializedName("companySupervisorEmail")
    private String companySupervisorEmail;

    @SerializedName("startDate")
    private Long startDate;

    @SerializedName("endDate")
    private Long endDate;

    public ConventionRequest() {}

    public ConventionRequest(Long studentId, Long pfaId, String companyName, String companyAddress,
                            String companySupervisorName, String companySupervisorEmail,
                            Long startDate, Long endDate) {
        this.studentId = studentId;
        this.pfaId = pfaId;
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.companySupervisorName = companySupervisorName;
        this.companySupervisorEmail = companySupervisorEmail;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getPfaId() { return pfaId; }
    public void setPfaId(Long pfaId) { this.pfaId = pfaId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }

    public String getCompanySupervisorName() { return companySupervisorName; }
    public void setCompanySupervisorName(String companySupervisorName) { this.companySupervisorName = companySupervisorName; }

    public String getCompanySupervisorEmail() { return companySupervisorEmail; }
    public void setCompanySupervisorEmail(String companySupervisorEmail) { this.companySupervisorEmail = companySupervisorEmail; }

    public Long getStartDate() { return startDate; }
    public void setStartDate(Long startDate) { this.startDate = startDate; }

    public Long getEndDate() { return endDate; }
    public void setEndDate(Long endDate) { this.endDate = endDate; }
}
