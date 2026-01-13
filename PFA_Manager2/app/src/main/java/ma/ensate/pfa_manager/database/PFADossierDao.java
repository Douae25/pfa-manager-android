package ma.ensate.pfa_manager.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;

@Dao
public interface PFADossierDao {
    @Query("DELETE FROM pfa_dossiers WHERE pfa_id NOT IN (:ids)")
    void deleteNotInIds(List<Long> ids);
    
    @Insert
    long insert(PFADossier dossier);

    @Insert
    void insertAll(List<PFADossier> dossiers);
    
    @Update
    void update(PFADossier dossier);
    
    @Update
    void updateAll(List<PFADossier> dossiers);
    
    @Delete
    void delete(PFADossier dossier);
    
    @Query("SELECT * FROM pfa_dossiers WHERE pfa_id = :id")
    PFADossier getById(long id);
    
    @Query("SELECT * FROM pfa_dossiers")
    List<PFADossier> getAll();
    
    @Query("SELECT * FROM pfa_dossiers WHERE student_id = :studentId")
    List<PFADossier> getByStudent(long studentId);
    
    @Query("SELECT * FROM pfa_dossiers WHERE supervisor_id = :supervisorId")
    List<PFADossier> getBySupervisor(long supervisorId);
    
    @Query("SELECT * FROM pfa_dossiers WHERE current_status = :status")
    List<PFADossier> getByStatus(PFAStatus status);

    @Query("DELETE FROM pfa_dossiers")
    void deleteAll();
}
