package ma.ensate.pfa_manager.database;

import androidx.lifecycle.LiveData;
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

    @Query("SELECT COUNT(*) FROM soutenances s " +
            "INNER JOIN pfa_dossiers p ON s.pfa_id = p.pfa_id " +
            "WHERE p.supervisor_id = :supervisorId AND s.status = 'PLANNED'")
    LiveData<Integer> countPlannedSoutenancesBySupervisor(Long supervisorId);

    @Query("SELECT s.* FROM soutenances s " +
            "INNER JOIN pfa_dossiers p ON s.pfa_id = p.pfa_id " +
            "WHERE p.supervisor_id = :supervisorId AND s.status = 'PLANNED' " +
            "ORDER BY s.date_soutenance ASC LIMIT 3")
    LiveData<List<Soutenance>> getUpcomingSoutenances(Long supervisorId);

    @Query("SELECT COUNT(*) FROM conventions c " +
            "INNER JOIN pfa_dossiers p ON c.pfa_id = p.pfa_id " +
            "WHERE p.supervisor_id = :supervisorId AND c.state = 'UPLOADED'")
    LiveData<Integer> countConventionsToValidate(Long supervisorId);
}
