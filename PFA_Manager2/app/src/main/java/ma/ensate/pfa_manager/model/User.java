package ma.ensate.pfa_manager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.TypeConverters;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

@Entity(
    tableName = "users",
    foreignKeys = @ForeignKey(
        entity = Department.class,
        parentColumns = "department_id",
        childColumns = "department_id",
        onDelete = ForeignKey.CASCADE // ou SET_NULL selon ton besoin
    )
)
public class User implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    @SerializedName("user_id")
    private Long user_id;

    @ColumnInfo(name = "email")
    @SerializedName("email")
    private String email;

    @ColumnInfo(name = "password")
    @SerializedName("password")
    private String password;

    @ColumnInfo(name = "first_name")
    @SerializedName("first_name")
    private String first_name;

    @ColumnInfo(name = "last_name")
    @SerializedName("last_name")
    private String last_name;

    @ColumnInfo(name = "role")
    @SerializedName("role")
    private Role role;

    @ColumnInfo(name = "phone_number")
    @SerializedName("phone_number")
    private String phone_number;

    // ID du département pour coordinateur et filtrage par département
    @ColumnInfo(name = "department_id")
    @SerializedName("department_id")
    private Long department_id;

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    private Long created_at;

    // Constructeur vide (requis par Room/Firebase)
    public User() {}

    // -------------------- Getters & Setters --------------------
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

    public Long getDepartment_id() { return department_id; }
    public void setDepartment_id(Long department_id) { this.department_id = department_id; }

    public Long getCreated_at() { return created_at; }
    public void setCreated_at(Long created_at) { this.created_at = created_at; }
}