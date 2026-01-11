package ma.ensate.pfa_manager.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ma.ensate.pfa_manager.model.EvaluationCriteria;

@Dao
public interface EvaluationCriteriaDao {

    @Insert
    long insert(EvaluationCriteria criteria);

    @Insert
    void insertAll(List<EvaluationCriteria> criteriaList);

    @Update
    void update(EvaluationCriteria criteria);

    @Delete
    void delete(EvaluationCriteria criteria);

    @Query("SELECT * FROM evaluation_criteria WHERE criteria_id = :id")
    EvaluationCriteria getById(long id);

    @Query("SELECT * FROM evaluation_criteria WHERE is_active = 1")
    List<EvaluationCriteria> getActive();

    @Query("SELECT * FROM evaluation_criteria WHERE is_active = 1")
    LiveData<List<EvaluationCriteria>> getActiveLive();

    @Query("SELECT * FROM evaluation_criteria")
    List<EvaluationCriteria> getAll();

    @Query("SELECT COUNT(*) FROM evaluation_criteria")
    int count();
}