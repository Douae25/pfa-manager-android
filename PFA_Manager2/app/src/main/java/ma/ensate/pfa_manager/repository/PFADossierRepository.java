package ma.ensate.pfa_manager.repository;

import android.app.Application;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.model.PFADossier;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PFADossierRepository {
    private PFADossierDao pfaDossierDao;
    private ExecutorService executorService;

    public interface PFADossierCallback {
        void onResult(PFADossier pfaDossier);
    }

    public interface PFADossierListCallback {
        void onResult(List<PFADossier> pfaDossiers);
    }

    public PFADossierRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        pfaDossierDao = db.pfaDossierDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(PFADossier pfaDossier, PFADossierCallback callback) {
        executorService.execute(() -> {
            long id = pfaDossierDao.insert(pfaDossier);
            pfaDossier.setPfa_id(id);
            if (callback != null) {
                callback.onResult(pfaDossier);
            }
        });
    }

    public void update(PFADossier pfaDossier, Runnable callback) {
        executorService.execute(() -> {
            pfaDossierDao.update(pfaDossier);
            if (callback != null) {
                callback.run();
            }
        });
    }

    public void delete(PFADossier pfaDossier, Runnable callback) {
        executorService.execute(() -> {
            pfaDossierDao.delete(pfaDossier);
            if (callback != null) {
                callback.run();
            }
        });
    }

    public void getById(Long pfaId, PFADossierCallback callback) {
        executorService.execute(() -> {
            PFADossier pfaDossier = pfaDossierDao.getById(pfaId);
            if (callback != null) {
                callback.onResult(pfaDossier);
            }
        });
    }

    public void getByStudentId(Long studentId, PFADossierCallback callback) {
        executorService.execute(() -> {
            List<PFADossier> dossiers = pfaDossierDao.getByStudent(studentId);
            PFADossier pfaDossier = (dossiers != null && !dossiers.isEmpty()) ? dossiers.get(0) : null;
            if (callback != null) {
                callback.onResult(pfaDossier);
            }
        });
    }

    public void getBySupervisorId(Long supervisorId, PFADossierListCallback callback) {
        executorService.execute(() -> {
            List<PFADossier> pfaDossiers = pfaDossierDao.getBySupervisor(supervisorId);
            if (callback != null) {
                callback.onResult(pfaDossiers);
            }
        });
    }

    public void getAll(PFADossierListCallback callback) {
        executorService.execute(() -> {
            List<PFADossier> pfaDossiers = pfaDossierDao.getAll();
            if (callback != null) {
                callback.onResult(pfaDossiers);
            }
        });
    }
}
