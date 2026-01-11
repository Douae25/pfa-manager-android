package ma.ensate.pfa_manager.repository;

import android.app.Application;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.DeliverableDao;
import ma.ensate.pfa_manager.model.Deliverable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeliverableRepository {
    
    private DeliverableDao deliverableDao;
    private ExecutorService executorService;
    
    public DeliverableRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        deliverableDao = database.deliverableDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public void insert(Deliverable deliverable, OnDeliverableInsertedListener listener) {
        executorService.execute(() -> {
            long id = deliverableDao.insert(deliverable);
            deliverable.setDeliverable_id(id);
            if (listener != null) {
                listener.onDeliverableInserted(deliverable);
            }
        });
    }
    
    public void update(Deliverable deliverable) {
        executorService.execute(() -> deliverableDao.update(deliverable));
    }
    
    public void delete(Deliverable deliverable) {
        executorService.execute(() -> deliverableDao.delete(deliverable));
    }
    
    public void getById(long id, OnDeliverableFetchedListener listener) {
        executorService.execute(() -> {
            Deliverable deliverable = deliverableDao.getById(id);
            if (listener != null) {
                listener.onDeliverableFetched(deliverable);
            }
        });
    }
    
    public void getByPfaId(long pfaId, OnDeliverablesListFetchedListener listener) {
        executorService.execute(() -> {
            List<Deliverable> deliverables = deliverableDao.getByPfaId(pfaId);
            if (listener != null) {
                listener.onDeliverablesListFetched(deliverables);
            }
        });
    }
    
    public void getAll(OnDeliverablesListFetchedListener listener) {
        executorService.execute(() -> {
            List<Deliverable> deliverables = deliverableDao.getAll();
            if (listener != null) {
                listener.onDeliverablesListFetched(deliverables);
            }
        });
    }
    
    public interface OnDeliverableInsertedListener {
        void onDeliverableInserted(Deliverable deliverable);
    }
    
    public interface OnDeliverableFetchedListener {
        void onDeliverableFetched(Deliverable deliverable);
    }
    
    public interface OnDeliverablesListFetchedListener {
        void onDeliverablesListFetched(List<Deliverable> deliverables);
    }
}
