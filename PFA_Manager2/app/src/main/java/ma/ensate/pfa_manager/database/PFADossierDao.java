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
    
    @Insert
    long insert(PFADossier dossier);
    
    @Update
    void update(PFADossier dossier);
    
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
}
