package ma.ensate.pfa_manager.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ma.ensate.pfa_manager.model.Evaluation;

@Dao
public interface EvaluationDao {

    @Insert
    long insert(Evaluation evaluation);

    @Update
    void update(Evaluation evaluation);

    @Delete
    void delete(Evaluation evaluation);

    @Query("SELECT * FROM evaluations WHERE evaluation_id = :id")
    Evaluation getById(long id);

    @Query("SELECT * FROM evaluations WHERE evaluation_id = :id")
    LiveData<Evaluation> getByIdLive(Long id);

    @Query("SELECT * FROM evaluations WHERE pfa_id = :pfaId")
    List<Evaluation> getByPfaId(long pfaId);

    @Query("SELECT * FROM evaluations WHERE pfa_id = :pfaId")
    LiveData<Evaluation> getByPfaIdLive(Long pfaId);

    @Query("SELECT * FROM evaluations WHERE evaluator_id = :evaluatorId")
    List<Evaluation> getByEvaluator(long evaluatorId);

    @Query("SELECT * FROM evaluations WHERE evaluator_id = :evaluatorId")
    LiveData<List<Evaluation>> getByEvaluatorLive(Long evaluatorId);

    @Query("SELECT * FROM evaluations")
    List<Evaluation> getAll();

    @Query("SELECT COUNT(*) FROM evaluations WHERE pfa_id = :pfaId")
    int countByPfaId(Long pfaId);

    @Query("SELECT COUNT(*) FROM evaluations e " +
            "INNER JOIN pfa_dossiers p ON e.pfa_id = p.pfa_id " +
            "WHERE p.supervisor_id = :supervisorId")
    LiveData<Integer> countBySupervisor(Long supervisorId);

    @Query("SELECT * FROM evaluations WHERE pfa_id = :pfaId LIMIT 1")
    Evaluation getByPfaIdSync(Long pfaId);

    @Query("SELECT * FROM evaluations WHERE pfa_id IN " +
            "(SELECT pfa_id FROM pfa_dossiers WHERE supervisor_id = :supervisorId)")
    List<Evaluation> getBySupervisorIdSync(Long supervisorId);

    @Query("SELECT COUNT(*) FROM pfa_dossiers " +
            "WHERE supervisor_id = :supervisorId " +
            "AND pfa_id NOT IN (SELECT pfa_id FROM evaluations)")
    LiveData<Integer> countUnevaluatedStudentsBySupervisor(Long supervisorId);
}