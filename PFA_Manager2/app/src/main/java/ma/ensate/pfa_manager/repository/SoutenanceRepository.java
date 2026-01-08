package ma.ensate.pfa_manager.repository;

import android.app.Application;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.SoutenanceDao;
import ma.ensate.pfa_manager.model.Soutenance;
import ma.ensate.pfa_manager.model.SoutenanceStatus;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoutenanceRepository {
    
    private SoutenanceDao soutenanceDao;
    private ExecutorService executorService;
    
    public SoutenanceRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        soutenanceDao = database.soutenanceDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public void insert(Soutenance soutenance, OnSoutenanceInsertedListener listener) {
        executorService.execute(() -> {
            long id = soutenanceDao.insert(soutenance);
            soutenance.setSoutenance_id(id);
            if (listener != null) {
                listener.onSoutenanceInserted(soutenance);
            }
        });
    }
    
    public void update(Soutenance soutenance) {
        executorService.execute(() -> soutenanceDao.update(soutenance));
    }
    
    public void delete(Soutenance soutenance) {
        executorService.execute(() -> soutenanceDao.delete(soutenance));
    }
    
    public void getById(long id, OnSoutenanceFetchedListener listener) {
        executorService.execute(() -> {
            Soutenance soutenance = soutenanceDao.getById(id);
            if (listener != null) {
                listener.onSoutenanceFetched(soutenance);
            }
        });
    }
    
    public void getByPfaId(long pfaId, OnSoutenanceFetchedListener listener) {
        executorService.execute(() -> {
            Soutenance soutenance = soutenanceDao.getByPfaId(pfaId);
            if (listener != null) {
                listener.onSoutenanceFetched(soutenance);
            }
        });
    }
    
    public void getByStatus(SoutenanceStatus status, OnSoutenancesListFetchedListener listener) {
        executorService.execute(() -> {
            List<Soutenance> soutenances = soutenanceDao.getByStatus(status);
            if (listener != null) {
                listener.onSoutenancesListFetched(soutenances);
            }
        });
    }
    
    public void getAll(OnSoutenancesListFetchedListener listener) {
        executorService.execute(() -> {
            List<Soutenance> soutenances = soutenanceDao.getAll();
            if (listener != null) {
                listener.onSoutenancesListFetched(soutenances);
            }
        });
    }
    
    public interface OnSoutenanceInsertedListener {
        void onSoutenanceInserted(Soutenance soutenance);
    }
    
    public interface OnSoutenanceFetchedListener {
        void onSoutenanceFetched(Soutenance soutenance);
    }
    
    public interface OnSoutenancesListFetchedListener {
        void onSoutenancesListFetched(List<Soutenance> soutenances);
    }
}
