package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class ConventionResponse {

    @SerializedName("conventionId")
    private Long conventionId;

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

    @SerializedName("scannedFileUri")
    private String scannedFileUri;

    @SerializedName("isValidated")
    private Boolean isValidated;

    @SerializedName("state")
    private String state;

    @SerializedName("adminComment")
    private String adminComment;

    // Getters
    public Long getConventionId() { return conventionId; }
    public Long getPfaId() { return pfaId; }
    public String getCompanyName() { return companyName; }
    public String getCompanyAddress() { return companyAddress; }
    public String getCompanySupervisorName() { return companySupervisorName; }
    public String getCompanySupervisorEmail() { return companySupervisorEmail; }
    public Long getStartDate() { return startDate; }
    public Long getEndDate() { return endDate; }
    public String getScannedFileUri() { return scannedFileUri; }
    public Boolean getIsValidated() { return isValidated; }
    public String getState() { return state; }
    public String getAdminComment() { return adminComment; }
}