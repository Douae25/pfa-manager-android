package ma.ensate.pfa_manager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;
import ma.ensate.pfa_manager.model.Department;
import ma.ensate.pfa_manager.model.DepartmentConverter;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    private Long user_id;
    
    @ColumnInfo(name = "email")
    private String email;
    
    @ColumnInfo(name = "password")
    private String password;
    
    @ColumnInfo(name = "first_name")
    private String first_name;
    
    @ColumnInfo(name = "last_name")
    private String last_name;
    
    @ColumnInfo(name = "role")
    private Role role;
    
    @ColumnInfo(name = "phone_number")
    private String phone_number;
    
    @ColumnInfo(name = "created_at")
    private Long created_at;

    @TypeConverters(DepartmentConverter.class)
    @ColumnInfo(name = "department_code")
    private Department department;

    // Constructeur vide (requis pour Firebase/Room)
    public User() {}

    // Getters et Setters
    public Long getUser_id() { return user_id; }
    public void setUser_id(Long user_id) { this.user_id = user_id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFirst_name() { return first_name; }
    public void setFirst_name(String first_name) { this.first_name = first_name; }
    
    public String getLast_name() { return last_name; }
    public void setLast_name(String last_name) { this.last_name = last_name; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public String getPhone_number() { return phone_number; }
    public void setPhone_number(String phone_number) { this.phone_number = phone_number; }
    
    public Long getCreated_at() { return created_at; }
    public void setCreated_at(Long created_at) { this.created_at = created_at; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
}