package ma.ensate.pfa_manager.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ma.ensate.pfa_manager.model.Department;

@Dao
public interface DepartmentDao {
    
    @Insert
    long insert(Department department);
    
    @Update
    void update(Department department);
    
    @Delete
    void delete(Department department);
    
    @Query("SELECT * FROM departments WHERE department_id = :id")
    Department getById(long id);
    
    @Query("SELECT * FROM departments")
    List<Department> getAll();
    
    @Query("SELECT * FROM departments WHERE code = :code LIMIT 1")
    Department getByCode(String code);
}
