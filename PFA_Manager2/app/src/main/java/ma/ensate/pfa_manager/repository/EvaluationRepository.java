package ma.ensate.pfa_manager.repository;

import android.app.Application;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.EvaluationDao;
import ma.ensate.pfa_manager.model.Evaluation;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EvaluationRepository {
    
    private EvaluationDao evaluationDao;
    private ExecutorService executorService;
    
    public EvaluationRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        evaluationDao = database.evaluationDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public void insert(Evaluation evaluation, OnEvaluationInsertedListener listener) {
        executorService.execute(() -> {
            long id = evaluationDao.insert(evaluation);
            evaluation.setEvaluation_id(id);
            if (listener != null) {
                listener.onEvaluationInserted(evaluation);
            }
        });
    }
    
    public void update(Evaluation evaluation) {
        executorService.execute(() -> evaluationDao.update(evaluation));
    }
    
    public void delete(Evaluation evaluation) {
        executorService.execute(() -> evaluationDao.delete(evaluation));
    }
    
    public void getById(long id, OnEvaluationFetchedListener listener) {
        executorService.execute(() -> {
            Evaluation evaluation = evaluationDao.getById(id);
            if (listener != null) {
                listener.onEvaluationFetched(evaluation);
            }
        });
    }
    
    public void getByPfaId(long pfaId, OnEvaluationsListFetchedListener listener) {
        executorService.execute(() -> {
            List<Evaluation> evaluations = evaluationDao.getByPfaId(pfaId);
            if (listener != null) {
                listener.onEvaluationsListFetched(evaluations);
            }
        });
    }
    
    public void getByEvaluator(long evaluatorId, OnEvaluationsListFetchedListener listener) {
        executorService.execute(() -> {
            List<Evaluation> evaluations = evaluationDao.getByEvaluator(evaluatorId);
            if (listener != null) {
                listener.onEvaluationsListFetched(evaluations);
            }
        });
    }
    
    public void getAll(OnEvaluationsListFetchedListener listener) {
        executorService.execute(() -> {
            List<Evaluation> evaluations = evaluationDao.getAll();
            if (listener != null) {
                listener.onEvaluationsListFetched(evaluations);
            }
        });
    }
    
    public interface OnEvaluationInsertedListener {
        void onEvaluationInserted(Evaluation evaluation);
    }
    
    public interface OnEvaluationFetchedListener {
        void onEvaluationFetched(Evaluation evaluation);
    }
    
    public interface OnEvaluationsListFetchedListener {
        void onEvaluationsListFetched(List<Evaluation> evaluations);
    }
}
