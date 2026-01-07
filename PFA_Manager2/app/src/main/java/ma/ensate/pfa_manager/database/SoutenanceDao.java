package ma.ensate.pfa_manager.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ma.ensate.pfa_manager.model.Soutenance;
import ma.ensate.pfa_manager.model.SoutenanceStatus;

@Dao
public interface SoutenanceDao {
    
    @Insert
    long insert(Soutenance soutenance);
    
    @Update
    void update(Soutenance soutenance);
    
    @Delete
    void delete(Soutenance soutenance);
    
    @Query("SELECT * FROM soutenances WHERE soutenance_id = :id")
    Soutenance getById(long id);
    
    @Query("SELECT * FROM soutenances WHERE pfa_id = :pfaId LIMIT 1")
    Soutenance getByPfaId(long pfaId);
    
    @Query("SELECT * FROM soutenances WHERE status = :status")
    List<Soutenance> getByStatus(SoutenanceStatus status);
    
    @Query("SELECT * FROM soutenances")
    List<Soutenance> getAll();
}
