// model/api/SoutenanceResponse.java
package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class SoutenanceResponse {
    @SerializedName("soutenanceId")
    private Long soutenanceId;

    @SerializedName("dateSoutenance")
    private Long dateSoutenance;

    @SerializedName("location")
    private String location;

    @SerializedName("status")
    private String status;

    // Getters
    public Long getSoutenanceId() { return soutenanceId; }
    public Long getDateSoutenance() { return dateSoutenance; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
}