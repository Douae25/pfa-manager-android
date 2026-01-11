package ma.ensate.pfa_manager.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.PFADossier;

public class PFADossierRepository {
    private final PFADossierDao pfaDossierDao;

    public PFADossierRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        pfaDossierDao = db.pfaDossierDao();
    }

    public LiveData<PFADossier> getPFADossierById(long pfaId) {
        MutableLiveData<PFADossier> liveData = new MutableLiveData<>();
        new Thread(() -> {
            PFADossier dossier = pfaDossierDao.getById(pfaId);
            liveData.postValue(dossier);
        }).start();
        return liveData;
    }

    // Ajoute une méthode callback pour compatibilité avec l'usage existant
    public void getPFADossierById(long pfaId, PFADossierCallback callback) {
        new Thread(() -> {
            PFADossier dossier = pfaDossierDao.getById(pfaId);
            callback.onPFADossierLoaded(dossier);
        }).start();
    }

    public interface PFADossierCallback {
        void onPFADossierLoaded(PFADossier pfaDossier);
    }
}
