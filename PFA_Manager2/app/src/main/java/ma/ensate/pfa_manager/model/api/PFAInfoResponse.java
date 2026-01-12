// model/api/PFAInfoResponse.java
package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class PFAInfoResponse {
    @SerializedName("pfaId")
    private Long pfaId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("currentStatus")
    private String currentStatus;

    @SerializedName("updatedAt")
    private Long updatedAt;

    // Getters
    public Long getPfaId() { return pfaId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCurrentStatus() { return currentStatus; }
    public Long getUpdatedAt() { return updatedAt; }
}