package ma.ensate.pfa_manager.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ma.ensate.pfa_manager.model.Convention;
import ma.ensate.pfa_manager.model.ConventionState;

@Dao
public interface ConventionDao {
    
    @Insert
    long insert(Convention convention);
    
    @Update
    void update(Convention convention);
    
    @Delete
    void delete(Convention convention);
    
    @Query("SELECT * FROM conventions WHERE convention_id = :id")
    Convention getById(long id);
    
    @Query("SELECT * FROM conventions WHERE pfa_id = :pfaId LIMIT 1")
    Convention getByPfaId(long pfaId);
    
    @Query("SELECT * FROM conventions WHERE state = :state")
    List<Convention> getByState(ConventionState state);
    
    @Query("SELECT * FROM conventions")
    List<Convention> getAll();
}
