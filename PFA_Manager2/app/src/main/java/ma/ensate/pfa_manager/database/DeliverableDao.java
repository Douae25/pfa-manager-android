package ma.ensate.pfa_manager.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ma.ensate.pfa_manager.model.Deliverable;

@Dao
public interface DeliverableDao {
    
    @Insert
    long insert(Deliverable deliverable);
    
    @Update
    void update(Deliverable deliverable);
    
    @Delete
    void delete(Deliverable deliverable);
    
    @Query("SELECT * FROM deliverables WHERE deliverable_id = :id")
    Deliverable getById(long id);
    
    @Query("SELECT * FROM deliverables WHERE pfa_id = :pfaId ORDER BY uploaded_at DESC")
    List<Deliverable> getByPfaId(long pfaId);
    
    @Query("SELECT * FROM deliverables")
    List<Deliverable> getAll();
}
