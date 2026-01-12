// model/api/SoutenanceRequest.java
package ma.ensate.pfa_manager.model.api;

import com.google.gson.annotations.SerializedName;

public class SoutenanceRequest {

    @SerializedName("pfaId")
    private Long pfaId;

    @SerializedName("location")
    private String location;

    @SerializedName("dateSoutenance")
    private Long dateSoutenance;

    public SoutenanceRequest(Long pfaId, String location, Long dateSoutenance) {
        this.pfaId = pfaId;
        this.location = location;
        this.dateSoutenance = dateSoutenance;
    }

    // Getters
    public Long getPfaId() { return pfaId; }
    public String getLocation() { return location; }
    public Long getDateSoutenance() { return dateSoutenance; }
}