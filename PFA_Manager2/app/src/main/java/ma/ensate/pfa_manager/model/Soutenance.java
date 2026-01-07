package ma.ensate.pfa_manager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "soutenances",
    foreignKeys = @ForeignKey(entity = PFADossier.class, parentColumns = "pfa_id", childColumns = "pfa_id"),
    indices = {@Index("pfa_id")})
public class Soutenance {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "soutenance_id")
    private Long soutenance_id;
    
    @ColumnInfo(name = "pfa_id")
    private Long pfa_id;
    
    @ColumnInfo(name = "location")
    private String location;
    
    @ColumnInfo(name = "date_soutenance")
    private Long date_soutenance;
    
    @ColumnInfo(name = "status")
    private SoutenanceStatus status;
    
    @ColumnInfo(name = "created_at")
    private Long created_at;
    
    public Soutenance() {}
    
    public Long getSoutenance_id() { return soutenance_id; }
    public void setSoutenance_id(Long soutenance_id) { this.soutenance_id = soutenance_id; }
    
    public Long getPfa_id() { return pfa_id; }
    public void setPfa_id(Long pfa_id) { this.pfa_id = pfa_id; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public Long getDate_soutenance() { return date_soutenance; }
    public void setDate_soutenance(Long date_soutenance) { this.date_soutenance = date_soutenance; }
    
    public SoutenanceStatus getStatus() { return status; }
    public void setStatus(SoutenanceStatus status) { this.status = status; }
    
    public Long getCreated_at() { return created_at; }
    public void setCreated_at(Long created_at) { this.created_at = created_at; }
}
