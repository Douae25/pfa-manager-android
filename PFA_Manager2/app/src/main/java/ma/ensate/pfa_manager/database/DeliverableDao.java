package ma.ensate.pfa_manager.database;

import androidx.lifecycle.LiveData;
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


    @Query("SELECT COUNT(*) FROM deliverables d " +
            "INNER JOIN pfa_dossiers p ON d.pfa_id = p.pfa_id " +
            "WHERE p.supervisor_id = :supervisorId")
    LiveData<Integer> countDeliverablesBySupervisor(Long supervisorId);

    @Query("SELECT * FROM deliverables WHERE pfa_id = :pfaId")
    LiveData<List<Deliverable>> getDeliverablesByPFA(Long pfaId);

    @Query("SELECT COUNT(*) FROM deliverables WHERE pfa_id = :pfaId")
    LiveData<Integer> countDeliverablesByPFA(Long pfaId);


    @Query("SELECT * FROM deliverables WHERE deliverable_id = :id")
    LiveData<Deliverable> getById(Long id);

    @Query("SELECT * FROM deliverables WHERE pfa_id = :pfaId ORDER BY uploaded_at DESC")
    LiveData<List<Deliverable>> getByPfaId(Long pfaId);

    @Query("SELECT * FROM deliverables WHERE pfa_id IN " +
            "(SELECT pfa_id FROM pfa_dossiers WHERE supervisor_id = :supervisorId) " +
            "ORDER BY uploaded_at DESC")
    LiveData<List<Deliverable>> getBySupervisorId(Long supervisorId);

    @Query("SELECT COUNT(*) FROM deliverables WHERE pfa_id IN " +
            "(SELECT pfa_id FROM pfa_dossiers WHERE supervisor_id = :supervisorId)")
    LiveData<Integer> getCountBySupervisor(Long supervisorId);

    @Query("SELECT COUNT(*) FROM deliverables WHERE pfa_id = :pfaId")
    LiveData<Integer> getCountByPfaId(Long pfaId);
}
