package ma.ensate.pfa_manager.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.Soutenance;
import ma.ensate.pfa_manager.model.SoutenanceStatus;

@Dao
public interface SoutenanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Soutenance soutenance);

    @Update
    void update(Soutenance soutenance);

    @Delete
    void delete(Soutenance soutenance);

    @Query("SELECT * FROM soutenances WHERE soutenance_id = :id")
    Soutenance getById(Long id);

    @Query("SELECT * FROM soutenances WHERE pfa_id = :pfaId LIMIT 1")
    Soutenance getByPfaId(long pfaId);

    @Query("SELECT * FROM soutenances WHERE pfa_id = :pfaId LIMIT 1")
    LiveData<Soutenance> getSoutenanceByPFA(Long pfaId);

    @Query("SELECT * FROM soutenances WHERE pfa_id = :pfaId LIMIT 1")
    Soutenance getByPfaIdSync(Long pfaId);

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

    @Query("SELECT s.* FROM soutenances s " +
            "INNER JOIN pfa_dossiers p ON s.pfa_id = p.pfa_id " +
            "WHERE p.supervisor_id = :supervisorId " +
            "ORDER BY s.date_soutenance ASC")
    LiveData<List<Soutenance>> getAllSoutenancesBySupervisor(Long supervisorId);

    @Query("SELECT * FROM pfa_dossiers " +
            "WHERE supervisor_id = :supervisorId " +
            "AND pfa_id NOT IN (SELECT pfa_id FROM soutenances)")
    LiveData<List<PFADossier>> getPFAsNonPlanifies(Long supervisorId);

    @Query("SELECT * FROM pfa_dossiers WHERE supervisor_id = :supervisorId")
    LiveData<List<PFADossier>> getAllPFAsBySupervisor(Long supervisorId);

    @Query("SELECT COUNT(*) FROM soutenances WHERE pfa_id = :pfaId")
    int countSoutenanceByPfa(Long pfaId);

    @Query("DELETE FROM soutenances WHERE soutenance_id = :id")
    void deleteById(Long id);

    @Query("SELECT COUNT(*) FROM soutenances")
    int getTotalCount();

    @Query("SELECT COUNT(*) FROM pfa_dossiers " +
            "WHERE supervisor_id = :supervisorId " +
            "AND pfa_id NOT IN (SELECT pfa_id FROM soutenances)")
    LiveData<Integer> countUnplannedSoutenancesBySupervisor(Long supervisorId);
}