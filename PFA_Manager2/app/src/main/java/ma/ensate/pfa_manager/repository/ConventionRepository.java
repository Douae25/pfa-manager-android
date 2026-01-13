package ma.ensate.pfa_manager.repository;

import android.app.Application;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.ConventionDao;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.model.Convention;
import ma.ensate.pfa_manager.model.ConventionState;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;
import android.util.Log;

public class ConventionRepository {

    private ConventionDao conventionDao;
    private PFADossierDao pfaDossierDao;
    private ExecutorService executorService;

    public ConventionRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        conventionDao = database.conventionDao();
        pfaDossierDao = database.pfaDossierDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Insert Convention
    public void insert(Convention convention, OnConventionInsertedListener listener) {
        executorService.execute(() -> {
            long id = conventionDao.insert(convention);
            convention.setConvention_id(id);
            if (listener != null) {
                listener.onConventionInserted(convention);
            }
        });
    }

    public void updateConvention(Convention convention) {
        executorService.execute(() -> {
            // Récupérer l'ancienne version pour comparer l'état
            Convention oldConvention = conventionDao.getById(convention.getConvention_id());
            
            // Mettre à jour la convention
            conventionDao.update(convention);
            
            // Si la convention passe à VALIDATED et qu'elle était UPLOADED
            if (convention.getState() == ConventionState.VALIDATED && 
                oldConvention != null && oldConvention.getState() == ConventionState.UPLOADED) {
                
                // Mettre à jour le statut du PFADossier associé à IN_PROGRESS
                if (convention.getPfa_id() != null) {
                    PFADossier dossier = pfaDossierDao.getById(convention.getPfa_id());
                    if (dossier != null) {
                        Log.d("ConventionRepository", "Convention validée - mise à jour du PFADossier " + dossier.getPfa_id() + " vers IN_PROGRESS");
                        dossier.setCurrent_status(PFAStatus.IN_PROGRESS);
                        pfaDossierDao.update(dossier);
                        Log.d("ConventionRepository", "PFADossier mis à jour avec succès");
                    }
                }
            }
        });
    }

    public void delete(Convention convention) {
        executorService.execute(() -> conventionDao.delete(convention));
    }

    public void getConventionById(long id, OnConventionFetchedListener listener) {
        executorService.execute(() -> {
            Convention convention = conventionDao.getById(id);
            if (listener != null) {
                listener.onConventionFetched(convention);
            }
        });
    }

    public void getConventionByPfaId(long pfaId, OnConventionFetchedListener listener) {
        executorService.execute(() -> {
            Convention convention = conventionDao.getByPfaId(pfaId);
            if (listener != null) {
                listener.onConventionFetched(convention);
            }
        });
    }

    public void getConventionsByState(ConventionState state, OnConventionsListFetchedListener listener) {
        executorService.execute(() -> {
            List<Convention> conventions = conventionDao.getByState(state);
            if (listener != null) {
                listener.onConventionsListFetched(conventions);
            }
        });
    }

    public void getAllConventions(OnConventionsListFetchedListener listener) {
        executorService.execute(() -> {
            List<Convention> conventions = conventionDao.getAll();
            if (listener != null) {
                listener.onConventionsListFetched(conventions);
            }
        });
    }

    public interface OnConventionInsertedListener {
        void onConventionInserted(Convention convention);
    }

    public interface OnConventionFetchedListener {
        void onConventionFetched(Convention convention);
    }

    public interface OnConventionsListFetchedListener {
        void onConventionsListFetched(List<Convention> conventions);
    }
}

