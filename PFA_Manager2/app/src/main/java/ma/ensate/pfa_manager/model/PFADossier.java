package ma.ensate.pfa_manager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "pfa_dossiers",
    foreignKeys = {
        @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "student_id"),
        @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "supervisor_id")
    },
    indices = {@Index("student_id"), @Index("supervisor_id")})
public class PFADossier {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pfa_id")
    private Long pfa_id;
    
    @ColumnInfo(name = "student_id")
    private Long student_id;
    
    @ColumnInfo(name = "supervisor_id")
    private Long supervisor_id;
    
    @ColumnInfo(name = "title")
    private String title;
    
    @ColumnInfo(name = "description")
    private String description;
    
    @ColumnInfo(name = "current_status")
    private PFAStatus current_status;
    
    @ColumnInfo(name = "updated_at")
    private Long updated_at;

    @ColumnInfo(name = "is_synced")
    private boolean is_synced = false;  // false = en attente de sync, true = synchronisé

    @ColumnInfo(name = "backend_pfa_id")
    private Long backend_pfa_id;  // ID retourné par le backend après sync
    
    public PFADossier() {}
    
    public Long getPfa_id() { return pfa_id; }
    public void setPfa_id(Long pfa_id) { this.pfa_id = pfa_id; }
    
    public Long getStudent_id() { return student_id; }
    public void setStudent_id(Long student_id) { this.student_id = student_id; }
    
    public Long getSupervisor_id() { return supervisor_id; }
    public void setSupervisor_id(Long supervisor_id) { this.supervisor_id = supervisor_id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public PFAStatus getCurrent_status() { return current_status; }
    public void setCurrent_status(PFAStatus current_status) { this.current_status = current_status; }
    
    public Long getUpdated_at() { return updated_at; }
    public void setUpdated_at(Long updated_at) { this.updated_at = updated_at; }

    public boolean isIs_synced() { return is_synced; }
    public void setIs_synced(boolean is_synced) { this.is_synced = is_synced; }

    public Long getBackend_pfa_id() { return backend_pfa_id; }
    public void setBackend_pfa_id(Long backend_pfa_id) { this.backend_pfa_id = backend_pfa_id; }
}
