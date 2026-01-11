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
import ma.ensate.pfa_manager.model.dto.StudentWithPFA;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    // ========== MÉTHODES SYNCHRONES (pour background thread) ==========

    @Query("SELECT * FROM users WHERE user_id = :userId")
    User getUserByIdSync(long userId);


    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmailSync(String email);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("DELETE FROM users")
    void deleteAll();

    // ========== MÉTHODES LIVEDATA (pour observation UI) ==========

    @Query("SELECT * FROM users WHERE user_id = :userId")
    LiveData<User> getUserById(Long userId);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    LiveData<User> getUserByEmail(String email);

    @Query("SELECT u.* FROM users u " +
            "INNER JOIN pfa_dossiers p ON u.user_id = p.student_id " +
            "WHERE p.supervisor_id = :supervisorId")
    LiveData<List<User>> getStudentsBySupervisor(Long supervisorId);

    // ========== MÉTHODES AVEC DTO (Relations complexes) ==========

    /**
     * Récupère tous les étudiants d'un superviseur avec leurs PFAs
     * Utilise @Transaction pour garantir la cohérence des données
     */
    @Transaction
    @Query("SELECT u.* FROM users u " +
            "INNER JOIN pfa_dossiers p ON u.user_id = p.student_id " +
            "WHERE p.supervisor_id = :supervisorId " +
            "ORDER BY u.last_name ASC, u.first_name ASC")
    LiveData<List<StudentWithPFA>> getStudentsWithPFABySupervisor(Long supervisorId);

    /**
     * Version synchrone pour background operations
     */
    @Transaction
    @Query("SELECT u.* FROM users u " +
            "INNER JOIN pfa_dossiers p ON u.user_id = p.student_id " +
            "WHERE p.supervisor_id = :supervisorId " +
            "ORDER BY u.last_name ASC, u.first_name ASC")
    List<StudentWithPFA> getStudentsWithPFABySupervisorSync(Long supervisorId);
}