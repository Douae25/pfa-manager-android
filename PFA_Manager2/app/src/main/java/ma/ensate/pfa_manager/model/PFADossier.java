package ma.ensate.pfa_manager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "pfa_dossiers",
    foreignKeys = {
        @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "student_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "supervisor_id", onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index("student_id"), @Index("supervisor_id")})
public class PFADossier {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pfa_id")
    @SerializedName("pfa_id")
    private Long pfa_id;
    
    @ColumnInfo(name = "student_id")
    @SerializedName("student_id")
    private Long student_id;
    
    @ColumnInfo(name = "supervisor_id")
    @SerializedName("supervisor_id")
    private Long supervisor_id;
    
    @ColumnInfo(name = "title")
    @SerializedName("title")
    private String title;
    
    @ColumnInfo(name = "description")
    @SerializedName("description")
    private String description;
    
    @ColumnInfo(name = "current_status")
    @SerializedName("current_status")
    private PFAStatus current_status;
    
    @ColumnInfo(name = "updated_at")
    @SerializedName("updatedAt")
    private Long updated_at;
    
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
}
