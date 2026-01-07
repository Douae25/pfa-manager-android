package ma.ensate.pfa_manager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "conventions",
    foreignKeys = @ForeignKey(entity = PFADossier.class, parentColumns = "pfa_id", childColumns = "pfa_id"),
    indices = {@Index("pfa_id")})
public class Convention {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "convention_id")
    private Long convention_id;
    
    @ColumnInfo(name = "pfa_id")
    private Long pfa_id;
    
    @ColumnInfo(name = "company_name")
    private String company_name;
    
    @ColumnInfo(name = "company_address")
    private String company_address;
    
    @ColumnInfo(name = "company_supervisor_name")
    private String company_supervisor_name;
    
    @ColumnInfo(name = "company_supervisor_email")
    private String company_supervisor_email;
    
    @ColumnInfo(name = "start_date")
    private Long start_date;
    
    @ColumnInfo(name = "end_date")
    private Long end_date;
    
    @ColumnInfo(name = "scanned_file_uri")
    private String scanned_file_uri;
    
    @ColumnInfo(name = "is_validated")
    private Boolean is_validated;
    
    @ColumnInfo(name = "state")
    private ConventionState state;
    
    @ColumnInfo(name = "admin_comment")
    private String admin_comment;
    
    public Convention() {}
    
    public Long getConvention_id() { return convention_id; }
    public void setConvention_id(Long convention_id) { this.convention_id = convention_id; }
    
    public Long getPfa_id() { return pfa_id; }
    public void setPfa_id(Long pfa_id) { this.pfa_id = pfa_id; }
    
    public String getCompany_name() { return company_name; }
    public void setCompany_name(String company_name) { this.company_name = company_name; }
    
    public String getCompany_address() { return company_address; }
    public void setCompany_address(String company_address) { this.company_address = company_address; }
    
    public String getCompany_supervisor_name() { return company_supervisor_name; }
    public void setCompany_supervisor_name(String company_supervisor_name) { this.company_supervisor_name = company_supervisor_name; }
    
    public String getCompany_supervisor_email() { return company_supervisor_email; }
    public void setCompany_supervisor_email(String company_supervisor_email) { this.company_supervisor_email = company_supervisor_email; }
    
    public Long getStart_date() { return start_date; }
    public void setStart_date(Long start_date) { this.start_date = start_date; }
    
    public Long getEnd_date() { return end_date; }
    public void setEnd_date(Long end_date) { this.end_date = end_date; }
    
    public String getScanned_file_uri() { return scanned_file_uri; }
    public void setScanned_file_uri(String scanned_file_uri) { this.scanned_file_uri = scanned_file_uri; }
    
    public Boolean getIs_validated() { return is_validated; }
    public void setIs_validated(Boolean is_validated) { this.is_validated = is_validated; }
    
    public ConventionState getState() { return state; }
    public void setState(ConventionState state) { this.state = state; }
    
    public String getAdmin_comment() { return admin_comment; }
    public void setAdmin_comment(String admin_comment) { this.admin_comment = admin_comment; }
}
