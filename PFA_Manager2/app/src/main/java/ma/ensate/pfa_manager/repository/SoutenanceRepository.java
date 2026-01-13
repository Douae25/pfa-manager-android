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
            Log.d(TAG, "📊 PFAs observés: " + (pfas != null ? pfas.size() : 0) +
                    ", Soutenances: " + (soutenances != null ? soutenances.size() : 0));
            if (pfas != null) {
                result.setValue(combine(pfas, soutenances));
            }
        });

        result.addSource(soutenancesLive, soutenances -> {
            List<PFADossier> pfas = pfasLive.getValue();
            Log.d(TAG, "📊 Soutenances observées: " + (soutenances != null ? soutenances.size() : 0) +
                    ", PFAs: " + (pfas != null ? pfas.size() : 0));
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
        Log.d(TAG, "🔄 Début sync soutenances pour superviseur: " + supervisorId);

        apiService.getPFAsWithSoutenances(supervisorId).enqueue(new Callback<ApiResponse<List<PFAWithSoutenanceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PFAWithSoutenanceResponse>>> call,
                                   Response<ApiResponse<List<PFAWithSoutenanceResponse>>> response) {

                Log.d(TAG, "📡 Réponse HTTP code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<PFAWithSoutenanceResponse>> apiResponse = response.body();
                    Log.d(TAG, "📡 API success: " + apiResponse.isSuccess());

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<PFAWithSoutenanceResponse> data = apiResponse.getData();
                        Log.d(TAG, "✅ Reçu " + data.size() + " PFAs de l'API");

                        // Log détaillé de chaque item
                        for (PFAWithSoutenanceResponse item : data) {
                            Log.d(TAG, "📦 PFA: id=" + item.getPfaId() + ", title=" + item.getTitle());
                            if (item.getSoutenance() != null) {
                                SoutenanceResponse s = item.getSoutenance();
                                Log.d(TAG, "   └── Soutenance: id=" + s.getSoutenanceId() +
                                        ", pfaId=" + s.getPfaId() +
                                        ", location=" + s.getLocation() +
                                        ", status=" + s.getStatus());
                            } else {
                                Log.d(TAG, "   └── Pas de soutenance");
                            }
                        }

                        saveSoutenancesLocally(data, supervisorId);
                    } else {
                        Log.w(TAG, "⚠️ Réponse API: success=false ou data=null");
                    }
                } else {
                    Log.e(TAG, "❌ Réponse non réussie: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "❌ Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Impossible de lire error body");
                        }
                    }
                }
                isSyncing.postValue(false);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PFAWithSoutenanceResponse>>> call, Throwable t) {
                Log.e(TAG, "❌ Erreur réseau: " + t.getMessage(), t);
                isSyncing.postValue(false);
            }
        });
    }

    private void saveSoutenancesLocally(List<PFAWithSoutenanceResponse> apiData, Long supervisorId) {
        executor.execute(() -> {
            Log.d(TAG, "💾 === DÉBUT SAUVEGARDE LOCALE ===");

            int savedCount = 0;
            int errorCount = 0;

            for (PFAWithSoutenanceResponse item : apiData) {
                Log.d(TAG, "🔍 Traitement PFA id=" + item.getPfaId());

                if (item.getSoutenance() == null) {
                    Log.d(TAG, "   ⏭️ Pas de soutenance, skip");
                    continue;
                }

                SoutenanceResponse sr = item.getSoutenance();

                try {
                    PFADossier pfaInDb = pfaDossierDao.getByIdSync(item.getPfaId());
                    if (pfaInDb == null) {
                        Log.w(TAG, "   ⚠️ PFA " + item.getPfaId() + " n'existe pas en local, impossible d'insérer la soutenance");
                        Log.w(TAG, "   💡 Vérifiez que StudentRepository sync les PFAs d'abord");
                        errorCount++;
                        continue;
                    }
                    Log.d(TAG, "   ✓ PFA trouvé en local: " + pfaInDb.getTitle());

                    // Vérifier si soutenance existe déjà
                    Soutenance existing = soutenanceDao.getByPfaIdSync(item.getPfaId());
                    Log.d(TAG, "   Soutenance existante: " + (existing != null ? "OUI (id=" + existing.getSoutenance_id() + ")" : "NON"));

                    // Créer ou mettre à jour
                    Soutenance soutenance;
                    if (existing != null) {
                        soutenance = existing;
                    } else {
                        soutenance = new Soutenance();
                    }

                    if (existing != null) {
                        soutenance.setSoutenance_id(existing.getSoutenance_id());
                    }
                    // Pour une nouvelle soutenance, laisser soutenance_id = null pour autoGenerate

                    soutenance.setPfa_id(sr.getPfaId() != null ? sr.getPfaId() : item.getPfaId());
                    soutenance.setLocation(sr.getLocation());
                    soutenance.setDate_soutenance(sr.getDateSoutenance());
                    soutenance.setStatus(mapStatus(sr.getStatus()));
                    soutenance.setCreated_at(sr.getCreatedAt() != null ? sr.getCreatedAt() : System.currentTimeMillis());

                    Log.d(TAG, "   📝 Soutenance à sauvegarder: pfa_id=" + soutenance.getPfa_id() +
                            ", location=" + soutenance.getLocation() +
                            ", status=" + soutenance.getStatus());

                    if (existing != null) {
                        soutenanceDao.update(soutenance);
                        Log.d(TAG, "   ✅ UPDATE réussi pour PFA " + item.getPfaId());
                    } else {
                        long newId = soutenanceDao.insert(soutenance);
                        Log.d(TAG, "   ✅ INSERT réussi pour PFA " + item.getPfaId() + ", nouvel ID=" + newId);
                    }
                    savedCount++;

                } catch (Exception e) {
                    Log.e(TAG, "   ❌ ERREUR pour PFA " + item.getPfaId() + ": " + e.getMessage(), e);
                    errorCount++;
                }
            }

            // Vérification finale
            int totalInDb = soutenanceDao.getTotalCount();
            Log.d(TAG, "💾 === FIN SAUVEGARDE ===");
            Log.d(TAG, "📊 Résultat: " + savedCount + " sauvegardées, " + errorCount + " erreurs");
            Log.d(TAG, "📊 Total soutenances en DB: " + totalInDb);
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

                // 2. NE PAS setter l'ID pour autoGenerate
                soutenance.setSoutenance_id(null);

                // 3. Insérer localement
                long id = soutenanceDao.insert(soutenance);
                soutenance.setSoutenance_id(id);
                Log.d(TAG, "✅ Soutenance insérée localement ID: " + id);

                // 4. Sync avec l'API en background
                syncPlanifierWithApi(supervisorId, soutenance, listener);

            } catch (Exception e) {
                Log.e(TAG, "❌ Erreur planification: " + e.getMessage(), e);
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

    // repository/SoutenanceRepository.java

// ═══════════════════════════════════════════════════════════════════════
// MODIFIER SOUTENANCE - CORRIGÉ
// ═══════════════════════════════════════════════════════════════════════

    public void modifierSoutenance(Soutenance soutenance, Long supervisorId, OnSoutenanceListener listener) {
        // ⚠️ Vérification null
        if (soutenance == null || soutenance.getSoutenance_id() == null) {
            listener.onError("Erreur: Soutenance invalide");
            return;
        }

        executor.execute(() -> {
            try {
                soutenanceDao.update(soutenance);
                Log.d(TAG, "✅ Soutenance modifiée localement, ID: " + soutenance.getSoutenance_id());

                // Sync API seulement si on a un ID valide
                syncModifierWithApi(supervisorId, soutenance, listener);

            } catch (Exception e) {
                Log.e(TAG, "❌ Erreur modification: " + e.getMessage());
                listener.onError("Erreur: " + e.getMessage());
            }
        });
    }

    private void syncModifierWithApi(Long supervisorId, Soutenance soutenance, OnSoutenanceListener listener) {
        Long soutenanceId = soutenance.getSoutenance_id();

        // ⚠️ Vérification avant appel API
        if (soutenanceId == null) {
            Log.w(TAG, "⚠️ ID null, sync API ignorée");
            listener.onSuccess("Soutenance modifiée (local uniquement)");
            return;
        }

        SoutenanceRequest request = new SoutenanceRequest(
                soutenance.getPfa_id(),
                soutenance.getLocation(),
                soutenance.getDate_soutenance()
        );

        Log.d(TAG, "📡 PUT /soutenances/" + soutenanceId);

        apiService.modifierSoutenance(soutenanceId, supervisorId, request)
                .enqueue(new Callback<ApiResponse<SoutenanceResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<SoutenanceResponse>> call,
                                           Response<ApiResponse<SoutenanceResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Log.d(TAG, "✅ Modification sync avec API");
                            listener.onSuccess("Soutenance modifiée avec succès !");
                        } else {
                            Log.w(TAG, "⚠️ API erreur: " + response.code());
                            listener.onSuccess("Soutenance modifiée localement");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<SoutenanceResponse>> call, Throwable t) {
                        Log.w(TAG, "⚠️ API sync failed: " + t.getMessage());
                        listener.onSuccess("Soutenance modifiée (mode hors-ligne)");
                    }
                });
    }

// ═══════════════════════════════════════════════════════════════════════
// SUPPRIMER SOUTENANCE - CORRIGÉ
// ═══════════════════════════════════════════════════════════════════════

    public void supprimerSoutenance(Long soutenanceId, Long supervisorId, OnSoutenanceListener listener) {
        // ⚠️ Vérification null
        if (soutenanceId == null) {
            listener.onError("Erreur: ID soutenance invalide");
            return;
        }

        executor.execute(() -> {
            try {
                soutenanceDao.deleteById(soutenanceId);
                Log.d(TAG, "✅ Soutenance supprimée localement, ID: " + soutenanceId);

                // Sync avec l'API
                Log.d(TAG, "📡 DELETE /soutenances/" + soutenanceId);

                apiService.supprimerSoutenance(soutenanceId, supervisorId)
                        .enqueue(new Callback<ApiResponse<Void>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "✅ Suppression sync avec API");
                                } else {
                                    Log.w(TAG, "⚠️ API erreur: " + response.code());
                                }
                                listener.onSuccess("Soutenance supprimée !");
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                                Log.w(TAG, "⚠️ API sync failed: " + t.getMessage());
                                listener.onSuccess("Soutenance supprimée (mode hors-ligne)");
                            }
                        });

            } catch (Exception e) {
                Log.e(TAG, "❌ Erreur suppression: " + e.getMessage());
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
                    if (s.getPfa_id() != null && s.getPfa_id().equals(pfa.getPfa_id())) {
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
            Log.w(TAG, "⚠️ Status inconnu: " + status);
            return SoutenanceStatus.PLANNED;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MÉTHODES EXISTANTES
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

    public void getByPfaId(Long pfaId, SoutenanceCallback callback) {
        executor.execute(() -> {
            Soutenance soutenance = soutenanceDao.getByPfaIdSync(pfaId);
            if (callback != null) {
                callback.onSoutenanceLoaded(soutenance);
            }
        });
    }

    public interface SoutenanceCallback {
        void onSoutenanceLoaded(Soutenance soutenance);
    }
}