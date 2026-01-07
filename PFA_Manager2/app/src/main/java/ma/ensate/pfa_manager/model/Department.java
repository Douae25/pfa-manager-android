package ma.ensate.pfa_manager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "departments")
public class Department {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "department_id")
    private Long department_id;
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "code")
    private String code;
    
    public Department() {}
    
    public Long getDepartment_id() { return department_id; }
    public void setDepartment_id(Long department_id) { this.department_id = department_id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
