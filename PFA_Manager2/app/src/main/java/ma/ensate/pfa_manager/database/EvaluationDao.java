package ma.ensate.pfa_manager.database;

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
    
    @Query("SELECT * FROM evaluations WHERE pfa_id = :pfaId")
    List<Evaluation> getByPfaId(long pfaId);
    
    @Query("SELECT * FROM evaluations WHERE evaluator_id = :evaluatorId")
    List<Evaluation> getByEvaluator(long evaluatorId);
    
    @Query("SELECT * FROM evaluations")
    List<Evaluation> getAll();
}
