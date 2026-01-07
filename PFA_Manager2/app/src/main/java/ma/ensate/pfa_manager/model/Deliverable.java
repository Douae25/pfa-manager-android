package ma.ensate.pfa_manager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "deliverables",
    foreignKeys = @ForeignKey(entity = PFADossier.class, parentColumns = "pfa_id", childColumns = "pfa_id"),
    indices = {@Index("pfa_id")})
public class Deliverable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "deliverable_id")
    private Long deliverable_id;
    
    @ColumnInfo(name = "pfa_id")
    private Long pfa_id;
    
    @ColumnInfo(name = "file_title")
    private String file_title;
    
    @ColumnInfo(name = "file_uri")
    private String file_uri;
    
    @ColumnInfo(name = "uploaded_at")
    private Long uploaded_at;
    
    public Deliverable() {}
    
    public Long getDeliverable_id() { return deliverable_id; }
    public void setDeliverable_id(Long deliverable_id) { this.deliverable_id = deliverable_id; }
    
    public Long getPfa_id() { return pfa_id; }
    public void setPfa_id(Long pfa_id) { this.pfa_id = pfa_id; }
    
    public String getFile_title() { return file_title; }
    public void setFile_title(String file_title) { this.file_title = file_title; }
    
    public String getFile_uri() { return file_uri; }
    public void setFile_uri(String file_uri) { this.file_uri = file_uri; }
    
    public Long getUploaded_at() { return uploaded_at; }
    public void setUploaded_at(Long uploaded_at) { this.uploaded_at = uploaded_at; }
}
