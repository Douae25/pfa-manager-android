package ma.ensate.pfa_manager.repository;

import android.app.Application;
import android.util.Log;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.PFAStatus;
import ma.ensate.pfa_manager.model.api.ApiResponse;
import ma.ensate.pfa_manager.model.api.PFADossierRequest;
import ma.ensate.pfa_manager.model.api.PFADossierResponse;
import ma.ensate.pfa_manager.network.ApiClient;
import ma.ensate.pfa_manager.network.ApiService;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Response;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.model.PFADossier;

public class PFADossierRepository {
    private PFADossierDao pfaDossierDao;
    private ExecutorService executorService;
    private ApiService apiService;

    public interface PFADossierCallback {
        void onResult(PFADossier pfaDossier);
    }

    public interface PFADossierListCallback {
        void onResult(List<PFADossier> pfaDossiers);
    }

    public interface PFADossierResponseCallback {
        void onResult(PFADossierResponse pfaDossierResponse);
    }


    public PFADossierRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        pfaDossierDao = db.pfaDossierDao();
        apiService = ApiClient.getApiService();
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

    // ════════════════════════════════════════════════════════════
    // API: Create or Get PFA Dossier
    // ════════════════════════════════════════════════════════════
    public void createOrGetPFADossier(PFADossierRequest request, PFADossierResponseCallback callback) {
        executorService.execute(() -> {
            try {
                Log.d("PFADossierAPI", "📤 Envoi requête createOrGet: studentId=" + request.getStudentId());
                
                // Appeler l'API
                Call<PFADossierResponse> call = apiService.createOrGetPFADossier(request);
                Response<PFADossierResponse> response = call.execute();
                
                Log.d("PFADossierAPI", "📥 Réponse reçue: code=" + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    PFADossierResponse pfaDossierResponse = response.body();
                    
                    // Synchroniser en Room
                    PFADossier pfaDossier = new PFADossier();
                    pfaDossier.setPfa_id(pfaDossierResponse.getPfaId());
                    pfaDossier.setStudent_id(pfaDossierResponse.getStudentId());
                    pfaDossier.setTitle(pfaDossierResponse.getTitle());
                    pfaDossier.setDescription(pfaDossierResponse.getDescription());
                    pfaDossier.setCurrent_status(PFAStatus.valueOf(pfaDossierResponse.getCurrentStatus()));
                    pfaDossier.setUpdated_at(pfaDossierResponse.getUpdatedAt());
                    
                    // Insérer ou mettre à jour en Room
                    try {
                        pfaDossierDao.insert(pfaDossier);
                    } catch (Exception e) {
                        // Si existe déjà, mettre à jour
                        pfaDossierDao.update(pfaDossier);
                    }
                    
                    Log.d("PFADossierAPI", "✅ Dossier synchronisé en Room: pfaId=" + pfaDossier.getPfa_id());
                    if (callback != null) {
                        callback.onResult(pfaDossierResponse);
                    }
                } else {
                    String errorMessage = "Erreur serveur: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        // Garder le message par défaut
                    }
                    Log.e("PFADossierAPI", "❌ Erreur API: " + errorMessage);
                    if (callback != null) {
                        callback.onResult(null);
                    }
                }
            } catch (Exception e) {
                // ═══════════════════════════════════════════════════
                // MODE OFFLINE: Erreur réseau → Créer localement
                // ═══════════════════════════════════════════════════
                Log.e("PFADossierAPI", "🔌 Erreur réseau (mode offline): " + e.getMessage());
                
                // Créer un dossier local avec état PENDING_SYNC
                PFADossier pfaDossierLocal = new PFADossier();
                pfaDossierLocal.setStudent_id(request.getStudentId());
                pfaDossierLocal.setTitle(request.getTitle());
                pfaDossierLocal.setDescription(request.getDescription());
                pfaDossierLocal.setCurrent_status(PFAStatus.CONVENTION_PENDING);
                pfaDossierLocal.setUpdated_at(System.currentTimeMillis());
                
                try {
                    // Insérer en Room
                    long pfaId = pfaDossierDao.insert(pfaDossierLocal);
                    pfaDossierLocal.setPfa_id(pfaId);
                    
                    // Créer une réponse fictive avec l'ID local
                    PFADossierResponse fakeResponse = new PFADossierResponse();
                    fakeResponse.setPfaId(pfaId);
                    fakeResponse.setStudentId(request.getStudentId());
                    fakeResponse.setTitle(request.getTitle());
                    fakeResponse.setDescription(request.getDescription());
                    fakeResponse.setCurrentStatus("CONVENTION_PENDING");
                    fakeResponse.setUpdatedAt(System.currentTimeMillis());
                    
                    Log.d("PFADossierAPI", "✅ Dossier créé localement (offline): pfaId=" + pfaId);
                    if (callback != null) {
                        callback.onResult(fakeResponse);
                    }
                } catch (Exception insertException) {
                    Log.e("PFADossierAPI", "❌ Erreur lors de l'insertion locale", insertException);
                    if (callback != null) {
                        callback.onResult(null);
                    }
                }
            }
        });
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
