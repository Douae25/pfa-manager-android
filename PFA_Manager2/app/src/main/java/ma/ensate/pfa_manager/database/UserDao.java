package ma.ensate.pfa_manager.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import ma.ensate.pfa_manager.model.User;
import ma.ensate.pfa_manager.model.Role;
import ma.ensate.pfa_manager.model.dto.StudentWithPFA;

@Dao
public interface UserDao {

    // ------------------- Méthodes classiques (synchro directe) -------------------
    @Insert
    long insert(User user);

    @Insert
    void insertAll(List<User> users);

    @Update
    void update(User user);

    @Update
    void updateAll(List<User> users);

    @Delete
    void delete(User user);

    @Delete
    void deleteAll(List<User> users);

    @Query("DELETE FROM users WHERE user_id NOT IN (:ids)")
    void deleteNotInIds(List<Long> ids);

    @Query("DELETE FROM users")
    void deleteAll();

    @Query("SELECT * FROM users WHERE user_id = :userId")
    User getUserByIdSync(long userId);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmailSync(String email);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT * FROM users WHERE department_id = :departmentId AND role = :role")
    List<User> getUsersByDepartmentAndRole(long departmentId, Role role);

    @Query("SELECT * FROM users WHERE department_id = :departmentId")
    List<User> getUsersByDepartment(long departmentId);

    // ------------------- Méthodes LiveData pour UI -------------------
    @Query("SELECT * FROM users WHERE user_id = :userId")
    LiveData<User> getUserById(Long userId);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    LiveData<User> getUserByEmailLiveData(String email); 

    @Query("SELECT u.* FROM users u " +
           "INNER JOIN pfa_dossiers p ON u.user_id = p.student_id " +
           "WHERE p.supervisor_id = :supervisorId")
    LiveData<List<User>> getStudentsBySupervisor(Long supervisorId);

    @Transaction
    @Query("SELECT u.* FROM users u " +
           "INNER JOIN pfa_dossiers p ON u.user_id = p.student_id " +
           "WHERE p.supervisor_id = :supervisorId " +
           "ORDER BY u.last_name ASC, u.first_name ASC")
    LiveData<List<StudentWithPFA>> getStudentsWithPFABySupervisor(Long supervisorId);

    @Transaction
    @Query("SELECT u.* FROM users u " +
           "INNER JOIN pfa_dossiers p ON u.user_id = p.student_id " +
           "WHERE p.supervisor_id = :supervisorId " +
           "ORDER BY u.last_name ASC, u.first_name ASC")
    List<StudentWithPFA> getStudentsWithPFABySupervisorSync(Long supervisorId);

    @Transaction
    @Query("SELECT * FROM users WHERE user_id = :studentId")
    LiveData<StudentWithPFA> getStudentWithPFAById(Long studentId);

}
