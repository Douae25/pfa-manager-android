package ma.ensate.pfa_manager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "departments")
public class Department {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "department_id")
    @SerializedName("department_id")
    private Long department_id;
    
    @ColumnInfo(name = "name")
    @SerializedName("name")
    private String name;
    
    @ColumnInfo(name = "code")
    @SerializedName("code")
    private String code;
    
    @Ignore
    public Department() {
        // Constructeur par défaut ignoré par Room
    }

    public Department(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public Long getDepartment_id() { return department_id; }
    public void setDepartment_id(Long department_id) { this.department_id = department_id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
