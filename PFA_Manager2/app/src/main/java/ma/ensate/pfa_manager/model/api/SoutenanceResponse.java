package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class SoutenanceResponse {

    @SerializedName("soutenanceId")
    private Long soutenanceId;

    @SerializedName("pfaId")
    private Long pfaId;

    @SerializedName("location")
    private String location;

    @SerializedName("dateSoutenance")
    private Long dateSoutenance;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private Long createdAt;

    // Getters
    public Long getSoutenanceId() { return soutenanceId; }
    public Long getPfaId() { return pfaId; }
    public String getLocation() { return location; }
    public Long getDateSoutenance() { return dateSoutenance; }
    public String getStatus() { return status; }
    public Long getCreatedAt() { return createdAt; }
}