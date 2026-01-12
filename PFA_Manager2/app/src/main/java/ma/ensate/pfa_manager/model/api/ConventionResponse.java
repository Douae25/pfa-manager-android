package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class ConventionResponse {
    @SerializedName("conventionId")
    private Long conventionId;

    @SerializedName("companyName")
    private String companyName;

    @SerializedName("companyAddress")
    private String companyAddress;

    @SerializedName("state")
    private String state;

    @SerializedName("scannedFileUri")
    private String scannedFileUri;

    // Getters
    public Long getConventionId() { return conventionId; }
    public String getCompanyName() { return companyName; }
    public String getCompanyAddress() { return companyAddress; }
    public String getState() { return state; }
    public String getScannedFileUri() { return scannedFileUri; }
}