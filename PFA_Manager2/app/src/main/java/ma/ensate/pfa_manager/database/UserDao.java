// ...existing code...
package ma.ensate.pfa_manager.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ma.ensate.pfa_manager.model.User;

@Dao
public interface UserDao {
    
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
    
    @Query("SELECT * FROM users WHERE user_id = :userId")
    User getUserById(long userId);
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);
    
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);
    
    @Query("SELECT * FROM users")
    List<User> getAllUsers();
    
    @Query("DELETE FROM users WHERE user_id NOT IN (:ids)")
    void deleteNotInIds(List<Long> ids);
}
