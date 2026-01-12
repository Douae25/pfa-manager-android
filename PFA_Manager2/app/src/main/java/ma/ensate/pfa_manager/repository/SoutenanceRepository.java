// repository/SoutenanceRepository.java (MISE À JOUR)
package ma.ensate.pfa_manager.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ma.ensate.pfa_manager.database.AppDatabase;
import ma.ensate.pfa_manager.database.SoutenanceDao;
import ma.ensate.pfa_manager.database.PFADossierDao;
import ma.ensate.pfa_manager.model.PFADossier;
import ma.ensate.pfa_manager.model.dto.PFAWithSoutenance;
import ma.ensate.pfa_manager.model.Soutenance;
import ma.ensate.pfa_manager.model.SoutenanceStatus;
import ma.ensate.pfa_manager.model.api.ApiResponse;
import ma.ensate.pfa_manager.model.api.PFAWithSoutenanceResponse;
import ma.ensate.pfa_manager.model.api.SoutenanceRequest;
import ma.ensate.pfa_manager.model.api.SoutenanceResponse;
import ma.ensate.pfa_manager.network.ApiClient;
import ma.ensate.pfa_manager.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SoutenanceRepository {

    private static final String TAG = "SoutenanceRepository";

    private final SoutenanceDao soutenanceDao;
    private final PFADossierDao pfaDossierDao;
    private final ApiService apiService;
    private final ExecutorService executor;

    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);

    public SoutenanceRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.soutenanceDao = db.soutenanceDao();
        this.pfaDossierDao = db.pfaDossierDao();
        this.apiService = ApiClient.getApiService();
        this.executor = Executors.newSingleThreadExecutor();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MÉTHODE PRINCIPALE : LiveData Room + Sync API
    // ═══════════════════════════════════════════════════════════════════════

    public LiveData<List<PFAWithSoutenance>> getPFAsWithSoutenances(Long supervisorId) {
        // 1. Sync depuis l'API en background
        syncFromApi(supervisorId);

        // 2. Retourner le LiveData combiné de Room
        MediatorLiveData<List<PFAWithSoutenance>> result = new MediatorLiveData<>();

        LiveData<List<PFADossier>> pfasLive = pfaDossierDao.getPFAsBySupervisor(supervisorId);
        LiveData<List<Soutenance>> soutenancesLive = soutenanceDao.getAllSoutenancesBySupervisor(supervisorId);

        result.addSource(pfasLive, pfas -> {
            List<Soutenance> soutenances = soutenancesLive.getValue();
            if (pfas != null) {
                result.setValue(combine(pfas, soutenances));
            }
        });

        result.addSource(soutenancesLive, soutenances -> {
            List<PFADossier> pfas = pfasLive.getValue();
            if (pfas != null) {
                result.setValue(combine(pfas, soutenances));
            }
        });

        return result;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SYNC DEPUIS L'API
    // ═══════════════════════════════════════════════════════════════════════

    public void syncFromApi(Long supervisorId) {
        isSyncing.postValue(true);
        Log.d(TAG, "🔄 Sync soutenances pour superviseur: " + supervisorId);

        apiService.getPFAsWithSoutenances(supervisorId).enqueue(new Callback<ApiResponse<List<PFAWithSoutenanceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PFAWithSoutenanceResponse>>> call,
                                   Response<ApiResponse<List<PFAWithSoutenanceResponse>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<PFAWithSoutenanceResponse>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        saveSoutenancesLocally(apiResponse.getData());
                        Log.d(TAG, "✅ Sync réussie: " + apiResponse.getData().size() + " PFAs");
                    }
                }
                isSyncing.postValue(false);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PFAWithSoutenanceResponse>>> call, Throwable t) {
                Log.e(TAG, "❌ Erreur sync: " + t.getMessage());
                isSyncing.postValue(false);
            }
        });
    }

    private void saveSoutenancesLocally(List<PFAWithSoutenanceResponse> apiData) {
        executor.execute(() -> {
            for (PFAWithSoutenanceResponse item : apiData) {
                try {
                    // Sauvegarder la soutenance si elle existe
                    if (item.getSoutenance() != null) {
                        SoutenanceResponse sr = item.getSoutenance();

                        Soutenance existing = soutenanceDao.getByPfaIdSync(item.getPfaId());
                        boolean isNew = (existing == null);

                        Soutenance soutenance = isNew ? new Soutenance() : existing;

                        soutenance.setSoutenance_id(sr.getSoutenanceId());
                        soutenance.setPfa_id(sr.getPfaId());
                        soutenance.setLocation(sr.getLocation());
                        soutenance.setDate_soutenance(sr.getDateSoutenance());
                        soutenance.setStatus(mapStatus(sr.getStatus()));
                        soutenance.setCreated_at(sr.getCreatedAt());

                        if (isNew) {
                            soutenanceDao.insert(soutenance);
                            Log.d(TAG, "✅ INSERT Soutenance PFA: " + item.getPfaId());
                        } else {
                            soutenanceDao.update(soutenance);
                            Log.d(TAG, "✅ UPDATE Soutenance PFA: " + item.getPfaId());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Erreur sauvegarde: " + e.getMessage());
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OPÉRATIONS CRUD (Local + Sync API)
    // ═══════════════════════════════════════════════════════════════════════

    public void planifierSoutenance(Soutenance soutenance, Long supervisorId, OnSoutenanceListener listener) {
        executor.execute(() -> {
            try {
                // 1. Vérifier localement
                if (soutenanceDao.countSoutenanceByPfa(soutenance.getPfa_id()) > 0) {
                    listener.onError("Une soutenance existe déjà pour ce PFA.");
                    return;
                }

                // 2. Insérer localement d'abord
                long id = soutenanceDao.insert(soutenance);
                soutenance.setSoutenance_id(id);
                Log.d(TAG, "✅ Soutenance insérée localement ID: " + id);

                // 3. Sync avec l'API en background
                syncPlanifierWithApi(supervisorId, soutenance, listener);

            } catch (Exception e) {
                listener.onError("Erreur: " + e.getMessage());
            }
        });
    }

    private void syncPlanifierWithApi(Long supervisorId, Soutenance soutenance, OnSoutenanceListener listener) {
        SoutenanceRequest request = new SoutenanceRequest(
                soutenance.getPfa_id(),
                soutenance.getLocation(),
                soutenance.getDate_soutenance()
        );

        apiService.planifierSoutenance(supervisorId, request).enqueue(new Callback<ApiResponse<SoutenanceResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<SoutenanceResponse>> call,
                                   Response<ApiResponse<SoutenanceResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Mettre à jour l'ID serveur si nécessaire
                    SoutenanceResponse sr = response.body().getData();
                    if (sr != null && sr.getSoutenanceId() != null) {
                        executor.execute(() -> {
                            soutenance.setSoutenance_id(sr.getSoutenanceId());
                            soutenanceDao.update(soutenance);
                        });
                    }
                    Log.d(TAG, "✅ Soutenance sync avec API");
                } else {
                    Log.w(TAG, "⚠️ API sync failed, données locales conservées");
                }
                listener.onSuccess("Soutenance planifiée avec succès !");
            }

            @Override
            public void onFailure(Call<ApiResponse<SoutenanceResponse>> call, Throwable t) {
                Log.w(TAG, "⚠️ API sync failed: " + t.getMessage() + ", données locales conservées");
                listener.onSuccess("Soutenance planifiée (mode hors-ligne)");
            }
        });
    }

    public void modifierSoutenance(Soutenance soutenance, Long supervisorId, OnSoutenanceListener listener) {
        executor.execute(() -> {
            try {
                // 1. Modifier localement
                soutenanceDao.update(soutenance);
                Log.d(TAG, "✅ Soutenance modifiée localement");

                // 2. Sync avec l'API
                syncModifierWithApi(supervisorId, soutenance, listener);

            } catch (Exception e) {
                listener.onError("Erreur: " + e.getMessage());
            }
        });
    }

    private void syncModifierWithApi(Long supervisorId, Soutenance soutenance, OnSoutenanceListener listener) {
        SoutenanceRequest request = new SoutenanceRequest(
                soutenance.getPfa_id(),
                soutenance.getLocation(),
                soutenance.getDate_soutenance()
        );

        apiService.modifierSoutenance(supervisorId, soutenance.getSoutenance_id(), request)
                .enqueue(new Callback<ApiResponse<SoutenanceResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<SoutenanceResponse>> call,
                                           Response<ApiResponse<SoutenanceResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Log.d(TAG, "✅ Modification sync avec API");
                        }
                        listener.onSuccess("Soutenance modifiée avec succès !");
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<SoutenanceResponse>> call, Throwable t) {
                        Log.w(TAG, "⚠️ API sync failed: " + t.getMessage());
                        listener.onSuccess("Soutenance modifiée (mode hors-ligne)");
                    }
                });
    }

    public void supprimerSoutenance(long soutenanceId, Long supervisorId, OnSoutenanceListener listener) {
        executor.execute(() -> {
            try {
                // 1. Supprimer localement
                soutenanceDao.deleteById(soutenanceId);
                Log.d(TAG, "✅ Soutenance supprimée localement");

                // 2. Sync avec l'API
                apiService.supprimerSoutenance(supervisorId, soutenanceId)
                        .enqueue(new Callback<ApiResponse<Void>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                                Log.d(TAG, "✅ Suppression sync avec API");
                                listener.onSuccess("Soutenance supprimée !");
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                                Log.w(TAG, "⚠️ API sync failed: " + t.getMessage());
                                listener.onSuccess("Soutenance supprimée (mode hors-ligne)");
                            }
                        });

            } catch (Exception e) {
                listener.onError("Erreur: " + e.getMessage());
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private List<PFAWithSoutenance> combine(List<PFADossier> pfas, List<Soutenance> soutenances) {
        List<PFAWithSoutenance> result = new ArrayList<>();
        for (PFADossier pfa : pfas) {
            PFAWithSoutenance item = new PFAWithSoutenance();
            item.pfa = pfa;
            item.soutenance = null;

            if (soutenances != null) {
                for (Soutenance s : soutenances) {
                    if (s.getPfa_id().equals(pfa.getPfa_id())) {
                        item.soutenance = s;
                        break;
                    }
                }
            }
            result.add(item);
        }
        return result;
    }

    private SoutenanceStatus mapStatus(String status) {
        if (status == null) return SoutenanceStatus.PLANNED;
        try {
            return SoutenanceStatus.valueOf(status);
        } catch (Exception e) {
            return SoutenanceStatus.PLANNED;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MÉTHODES EXISTANTES (gardées pour compatibilité)
    // ═══════════════════════════════════════════════════════════════════════

    public LiveData<List<PFADossier>> getPFAsEligibles(Long supervisorId) {
        return soutenanceDao.getPFAsNonPlanifies(supervisorId);
    }

    public LiveData<List<PFADossier>> getAllPFAsBySupervisor(Long supervisorId) {
        return soutenanceDao.getAllPFAsBySupervisor(supervisorId);
    }

    public LiveData<List<Soutenance>> getAllSoutenancesBySupervisor(Long supervisorId) {
        return soutenanceDao.getAllSoutenancesBySupervisor(supervisorId);
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    public interface OnSoutenanceListener {
        void onSuccess(String message);
        void onError(String message);
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

    public interface SoutenanceCallback {
        void onSoutenanceLoaded(Soutenance soutenance);
    }

    public void getByPfaId(Long pfaId, SoutenanceCallback callback) {
        executor.execute(() -> {
            Soutenance soutenance = soutenanceDao.getByPfaIdSync(pfaId);
            if (callback != null) {
                callback.onSoutenanceLoaded(soutenance);
            }
        });
    }
}