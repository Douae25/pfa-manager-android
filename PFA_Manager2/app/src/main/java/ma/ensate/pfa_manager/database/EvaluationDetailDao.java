package ma.ensate.pfa_manager.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ma.ensate.pfa_manager.model.EvaluationDetail;

@Dao
public interface EvaluationDetailDao {

    @Insert
    long insert(EvaluationDetail detail);

    @Insert
    void insertAll(List<EvaluationDetail> details);

    @Update
    void update(EvaluationDetail detail);

    @Delete
    void delete(EvaluationDetail detail);

    @Query("SELECT * FROM evaluation_details WHERE detail_id = :id")
    EvaluationDetail getById(long id);

    @Query("SELECT * FROM evaluation_details WHERE evaluation_id = :evaluationId")
    List<EvaluationDetail> getByEvaluationId(long evaluationId);

    @Query("SELECT * FROM evaluation_details WHERE evaluation_id = :evaluationId")
    LiveData<List<EvaluationDetail>> getByEvaluationIdLive(Long evaluationId);

    @Query("SELECT * FROM evaluation_details")
    List<EvaluationDetail> getAll();

    @Query("DELETE FROM evaluation_details WHERE evaluation_id = :evaluationId")
    void deleteByEvaluationId(Long evaluationId);
}